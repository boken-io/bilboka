package ivaralek.bilboka.book.domain

import java.time.LocalDate

class MaintenanceRecord(date: LocalDate?, odometer: Int?) : Record(date, RecordType.MAINTENANCE, odometer) {

}