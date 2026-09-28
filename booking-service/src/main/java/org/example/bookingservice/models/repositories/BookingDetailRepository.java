package org.example.bookingservice.models.repositories;

import org.example.bookingservice.models.entities.BookingDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingDetailRepository extends JpaRepository<BookingDetail, Long> {

    List<BookingDetail> findAllByBookingIdOrderByIdAsc(Long bookingId);
}
