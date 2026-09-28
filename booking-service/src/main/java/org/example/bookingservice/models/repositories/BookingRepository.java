package org.example.bookingservice.models.repositories;

import org.example.bookingservice.models.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByIdDesc();

    List<Booking> findAllByCustomerEmailIgnoreCaseOrderByIdDesc(String customerEmail);
}
