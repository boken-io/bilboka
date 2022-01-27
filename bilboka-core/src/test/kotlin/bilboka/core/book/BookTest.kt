package bilboka.core.book

import bilboka.core.book.domain.Book
import bilboka.core.book.domain.FuelRecord
import bilboka.core.book.domain.MaintenanceRecord
import bilboka.core.vehicle.Vehicle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate.now

internal class BookTest {

    @Test
    fun addRecordAddsRecord() {
        val book = Book(Vehicle(""))

        var record1 = FuelRecord(now(), 23300, null, null, true)
        var record2 = MaintenanceRecord(now().minusDays(1), 40000)

        book.addRecord(record1)
        book.addRecord(record2)

        assertThat(book.records).contains(record1)
        assertThat(book.records).contains(record2)
        assertThat(book.records).hasSize(2)
    }
}
