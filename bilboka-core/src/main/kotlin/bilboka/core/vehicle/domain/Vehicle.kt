package bilboka.core.vehicle.domain

import bilboka.core.book.MaintenanceItemMissingException
import bilboka.core.book.domain.*
import bilboka.core.fuelestimation.ConsumptionEstimationResult
import bilboka.core.fuelestimation.ConsumptionEstimator
import bilboka.core.user.domain.User
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant
import java.time.LocalDateTime

object Vehicles : IntIdTable() {
    val name = varchar("name", 50).uniqueIndex()
    val nicknames = text("nicknames").default("")
    val tegnkombinasjonNormalisert = varchar("tegnkombinasjon_normalisert", 15).nullable()
    val odometerUnit = enumerationByName("odo_unit", 20, OdometerUnit::class).nullable()
    val fuelType = enumerationByName("fuel_type", 15, FuelType::class).nullable()
    val tankVolume = integer("tank_volume").nullable()
    val creationTimestamp = timestamp("created_timestamp").clientDefault { Instant.now() }
}

class Vehicle(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<Vehicle>(Vehicles, Vehicle::class.java) {
        const val SEPARATOR = "|"
    }

    var name by Vehicles.name
    var nicknames by Vehicles.nicknames.transform(
        { a -> a.joinToString(SEPARATOR) },
        { str -> str.split(SEPARATOR).toSet() }
    )
    var tegnkombinasjonNormalisert by Vehicles.tegnkombinasjonNormalisert
    var odometerUnit by Vehicles.odometerUnit
    var fuelType by Vehicles.fuelType
    var tankVolume by Vehicles.tankVolume
    val bookEntries by BookEntry referrersOn BookEntries.vehicle
    val creationTimestamp by Vehicles.creationTimestamp

    fun addFuel(
        odometer: Int?,
        amount: Double?,
        costNOK: Double?,
        isFull: Boolean = false,
        enteredBy: User? = null,
        source: String,
        dateTime: LocalDateTime? = LocalDateTime.now()
    ): BookEntry {
        val thisVehicle = this
        return transaction {
            BookEntry.new {
                this.dateTime = dateTime
                this.type = EntryType.FUEL
                this.enteredBy = enteredBy
                this.odometer = odometer
                this.vehicle = thisVehicle
                this.amount = amount
                this.costNOK = costNOK
                this.isFullTank = isFull
                this.source = source
            }
        }
    }

    fun enterMaintenance(
        maintenanceItem: String,
        odometer: Int?,
        amount: Double? = null,
        costNOK: Double? = null,
        comment: String? = null,
        enteredBy: User? = null,
        source: String,
        dateTime: LocalDateTime? = LocalDateTime.now(),
        createIfMissing: Boolean = false
    ): BookEntry {
        val thisVehicle = this
        return transaction {
            val maintenanceEntity = MaintenanceItems.getItem(maintenanceItem).let {
                if (it == null && createIfMissing) MaintenanceItem.new { item = maintenanceItem } else it
            } ?: throw MaintenanceItemMissingException(maintenanceItem)
            BookEntry.new {
                this.dateTime = dateTime
                this.maintenanceItem = maintenanceEntity
                this.comment = comment
                this.type = EntryType.MAINTENANCE
                this.enteredBy = enteredBy
                this.odometer = odometer
                this.vehicle = thisVehicle
                this.amount = amount
                this.costNOK = costNOK
                this.source = source
            }
        }
    }

    fun lastEntry(): BookEntry? {
        return transaction {
            datedEntries()
                .maxByOrNull { it.dateTime!! }
        }
    }

    fun lastOdometer(): Int? {
        return transaction {
            datedEntries()
                .filter { it.odometer != null }
                .maxByOrNull { it.dateTime!! }?.odometer
        }
    }

    fun lastEntry(type: EntryType): BookEntry? {
        return transaction {
            datedEntries().filter { it.type == type }
                .maxByOrNull { it.dateTime!! }
        }
    }

    fun lastMaintenance(maintenanceItem: String): BookEntry? {
        return transaction {
            val maintenanceOfRightType = bookEntries
                .filter { it.type == EntryType.MAINTENANCE }
                .filter { it.maintenanceItem == MaintenanceItems.getItem(maintenanceItem) }

            val lastWithDate = maintenanceOfRightType.filter { it.dateTime != null }.maxByOrNull { it.dateTime!! }
            val lastWithOdo = maintenanceOfRightType.filter { it.odometer != null }.maxByOrNull { it.odometer!! }
            lastWithDate?.let {
                if (it.odometer != null && it.odometer!! < (lastWithOdo?.odometer ?: 0)) lastWithOdo else it
            } ?: lastWithOdo
        }
    }

    fun lastConsumptionEstimate(): ConsumptionEstimationResult? {
        return ConsumptionEstimator.lastEstimate(bookEntries.toList(), odometerUnit)
    }

    private fun datedEntries(): List<BookEntry> {
        return bookEntries.filter { it.dateTime != null }
    }

    fun fuelType(): FuelType {
        return fuelType ?: throw IllegalStateException("Kjøretøy mangler fueltype")
    }

    fun isCalled(calledName: String): Boolean {
        return calledName.normalizeName() == name.normalizeName()
                || nicknames.map { it.normalizeName() }.contains(calledName.normalizeName())
                || hasTegnkombinasjon(calledName)
    }

    fun hasTegnkombinasjon(tegnkombinasjon: String): Boolean {
        return tegnkombinasjon.normaliserTegnkombinasjon() == tegnkombinasjonNormalisert
    }

    fun enterComment(
        comment: String,
        odometer: Int? = null,
        costNOK: Double? = null,
        enteredBy: User? = null,
        source: String,
        dateTime: LocalDateTime? = LocalDateTime.now(),
    ): BookEntry {
        val thisVehicle = this
        return transaction {
            BookEntry.new {
                this.dateTime = dateTime
                this.comment = comment
                this.type = EntryType.BASIC
                this.enteredBy = enteredBy
                this.odometer = odometer
                this.vehicle = thisVehicle
                this.costNOK = costNOK
                this.source = source
            }
        }
    }
}

fun String.normaliserTegnkombinasjon(): String {
    return this.uppercase().replace(" ", "").replace("-", "")
}

fun String.normalizeName(): String {
    return this.lowercase().replace(" ", "")
}
