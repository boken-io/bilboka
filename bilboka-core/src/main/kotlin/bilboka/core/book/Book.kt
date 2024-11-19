package bilboka.core.book

import bilboka.core.book.domain.*
import bilboka.core.report.ReportGenerator
import bilboka.core.user.domain.User
import bilboka.core.vehicle.VehicleService
import bilboka.core.vehicle.domain.FuelType
import bilboka.core.vehicle.domain.Vehicle
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Year
import kotlin.math.sign

@Component
class Book(
    private val vehicleService: VehicleService,
    private val reportGenerator: ReportGenerator
) {

    fun addFuelForVehicle(
        vehicleName: String,
        dateTime: LocalDateTime? = LocalDateTime.now(),
        odoReading: Int?,
        amount: Double?,
        costNOK: Double?,
        enteredBy: User? = null,
        source: String,
        isFull: Boolean = false
    ): BookEntry {
        val vehicle = vehicleService.getVehicle(vehicleName)
        validateFuelRequest(vehicle, dateTime, odoReading, amount, costNOK)
        return vehicle.addFuel(
            enteredBy = enteredBy,
            dateTime = dateTime,
            odometer = odoReading,
            amount = amount,
            costNOK = costNOK,
            isFull = isFull,
            source = source
        )
    }

    private fun validateFuelRequest(
        vehicle: Vehicle,
        dateTime: LocalDateTime?,
        odoReading: Int?,
        amount: Double?,
        costNOK: Double?
    ) {
        vehicle.lastEntry(EntryType.FUEL)?.checkIfDuplicate(odoReading, amount, costNOK)
        odoReading?.validateAsOdometer()
        amount?.validateAsAmount()
        costNOK?.validateAsCost()
        vehicle.lastEntry()?.checkChronologyAgainst(dateTime, odoReading)
    }

    fun setIsFullTank(vehicleName: String, odoReading: Int): BookEntry? {
        return transaction {
            vehicleService.getVehicle(vehicleName).bookEntries
                .find { it.odometer == odoReading && it.type == EntryType.FUEL }
                ?.apply { isFullTank = true }
        }
    }

    fun getLastFuelEntry(vehicle: String): BookEntry? {
        return vehicleService.getVehicle(vehicle).lastEntry(EntryType.FUEL)
    }

    fun getLastFuelPrices(n: Int = 5, fuelType: FuelType? = null): List<Pair<LocalDate, Double>> {
        return transaction {
            BookEntry
                .find { BookEntries.type eq EntryType.FUEL }
                .asSequence()
                .sortedByDescending { it.dateTime }
                .filter { it.dateTime != null }
                .filter { it.pricePerLiter() != null }
                .filter { fuelType?.run { it.vehicle.fuelType == this } ?: true }
                .take(n)
                .map { Pair(it.dateTime!!.toLocalDate(), it.pricePerLiter() as Double) }
                .toList()
        }
    }

    fun maintenanceItems(): Set<String> {
        return transaction {
            MaintenanceItem.all().map { it.item }
        }.toSet()
    }

    fun addMaintenanceItem(newItem: String) {
        transaction {
            MaintenanceItem.new { item = newItem }
        }
    }

    fun getMaintenanceReport(vehicle: Vehicle): ByteArray? {
        return reportIfNotEmpty(
            header = "Vedlikeholdsrapport for ${vehicle.name}${vehicle.tegnkombinasjonVisning?.let { " ($it)" } ?: ""}",
            entries = vehicle.bookEntries.filter { it.type != EntryType.FUEL }
        )
    }

    fun getReport(vehicle: Vehicle, year: Int? = null): ByteArray? {
        return if (year != null) reportOfYear(vehicle, Year.of(year)) else reportOfLastEntries(vehicle)
    }

    private fun reportOfYear(vehicle: Vehicle, year: Year): ByteArray? {
        return reportIfNotEmpty(
            header = "Rapport for $year, ${vehicle.name}${vehicle.tegnkombinasjonVisning?.let { " ($it)" } ?: ""}",
            entries = vehicle.bookEntries.between(year.atDay(1), year.plusYears(1).atDay(1))
        )
    }

    private fun reportOfLastEntries(vehicle: Vehicle): ByteArray? {
        val lastYear = vehicle.bookEntries.since(LocalDate.now().minusYears(1))
        val last10k = vehicle.lastOdometer()?.minus(10000)?.let { vehicle.bookEntries.since(it) } ?: listOf()
        return if (lastYear.size > last10k.size)
            reportIfNotEmpty(
                header = "Rapport for siste år, ${vehicle.name}${vehicle.tegnkombinasjonVisning?.let { " ($it)" } ?: ""}",
                entries = lastYear
            ) else reportIfNotEmpty(
            header = "Rapport for siste 10 000 km, ${vehicle.name}${vehicle.tegnkombinasjonVisning?.let { " ($it)" } ?: ""}",
            entries = last10k
        )
    }

    private fun reportIfNotEmpty(header: String, entries: List<BookEntry>): ByteArray? {
        return entries.takeIf { it.isNotEmpty() }?.let {
            reportGenerator.generateReport(
                header = header,
                entries = entries
            )
        }
    }
}

private fun BookEntry.checkIfDuplicate(odoReading: Int?, amount: Double?, costNOK: Double?) {
    if (this.odometer == odoReading
        && this.amount == amount
        && this.costNOK == costNOK
    ) {
        throw DuplicateBookEntryException()
    }
}

fun Double.validateAsAmount() {
    if (this > 1000) {
        throw BookEntryException("Usannsynlig verdi for mengde.")
    }
}

fun Double.validateAsCost() {
    if (this > 10000) {
        throw BookEntryException("Usannsynlig verdi for kostnad.")
    }
}

fun Int.validateAsOdometer() {
    if (this > 10000000) {
        throw BookEntryException("Usannsynlig verdi for kilometerstand.")
    }
}

private fun BookEntry.checkChronologyAgainst(dateTime: LocalDateTime?, odoReading: Int?) {
    if (odoReading != null && this.odometer != null
        && this.dateTime?.compareTo(dateTime ?: LocalDateTime.now())?.sign != this.odometer?.compareTo(odoReading)?.sign
    ) {
        throw BookEntryChronologyException("Angitt kilometerstand er ikke i kronologisk rekkefølge med tidligere angitt.")
    }
}

fun String.toMaintenanceItem(): String {
    return this.normalizeAsMaintenanceItem()
}

private fun SizedIterable<BookEntry>.since(date: LocalDate): List<BookEntry> {
    return firstIndexAfter(date.atStartOfDay())
        ?.let { filterIndexed { index, _ -> index >= it } } ?: emptyList()
}

private fun SizedIterable<BookEntry>.since(odo: Int): List<BookEntry> {
    return firstIndexAfter(odo)
        ?.let { filterIndexed { index, _ -> index >= it } } ?: emptyList()
}

private fun SizedIterable<BookEntry>.between(from: LocalDate, to: LocalDate): List<BookEntry> {
    val firstEntryIncluded = firstIndexAfter(from.atStartOfDay())
    val firstEntryNotIncluded = firstIndexAfter(to.atStartOfDay())

    return if (firstEntryIncluded == null) {
        emptyList()
    } else if (firstEntryNotIncluded == null) {
        filterIndexed { index, _ -> index >= firstEntryIncluded }
    } else {
        filterIndexed { index, _ -> index >= firstEntryIncluded && index < firstEntryNotIncluded }
    }
}

private fun SizedIterable<BookEntry>.firstIndexAfter(date: LocalDateTime): Int? {
    sorted().indexOfFirst { it.dateTime?.run { this > date } == true }
    return null
}

private fun SizedIterable<BookEntry>.firstIndexAfter(odo: Int): Int? {
    sorted().indexOfFirst { it.odometer?.run { this > odo } == true }
    return null
}
