package bilboka.client

import java.time.LocalDate

data class VehicleResponse(
    val id: String,
    val name: String,
    @Deprecated("Use visning or normalisert instead")
    val tegnkombinasjon: String? = null,
    val tegnkombinasjonNormalisert: String? = null,
    val tegnkombinasjonVisning: String?,
    val merke: String? = null,
    val modell: String? = null,
    val odometerUnit: String?,
    val fuelType: String?,
    val tankVolume: Int? = null,
    val lastOdometer: Int? = null,
    val lastOdometerKilometers: Int? = null,
    val lastYearlyDifference: BilbokaDataPoint? = null,
    val averageFuelConsumption: BilbokaDataPoint? = null,
    val entriesCount: Int,
    val understellsnummer: String? = null,
    val regStatus: String? = null,
    val sistePKK: LocalDate? = null,
    val fristPKK: LocalDate? = null,
    val regBevaringsverdig: Boolean = false,
    val egenvekt: Int? = null,
    val nyttelast: Int? = null,
    val hengervektMBrems: Int? = null,
    val lengde: Int? = null,
)
