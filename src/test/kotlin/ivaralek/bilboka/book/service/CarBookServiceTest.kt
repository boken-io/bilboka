package ivaralek.bilboka.book.service

import ivaralek.bilboka.book.domain.FuelRecord
import ivaralek.bilboka.book.repository.InMemoryStorage
import ivaralek.bilboka.vehicle.Vehicle
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate.now

internal class CarBookServiceTest {

    val carBookService: CarBookService = CarBookService(InMemoryStorage())

    @Test
    fun makeNewBook() {

        val bil = Vehicle("Testbil")
        val newBook = carBookService.makeNewBookForVehicle(bil)

        assertThat(newBook.vehicle).isEqualTo(bil)
        assertThat(carBookService.getBookForVehicle(bil)).isNotNull

    }

    @Test
    fun addFuelRecordToBook() {
        val bil = Vehicle("Testbil")
        carBookService.makeNewBookForVehicle(bil)

        val fuelToAdd = FuelRecord(now(), 300000, 12.4, 13.37, false)

        carBookService.addRecordForVehicle(fuelToAdd, bil)

        assertThat(carBookService.getBookForVehicle(bil)?.records).contains(fuelToAdd)
    }
}