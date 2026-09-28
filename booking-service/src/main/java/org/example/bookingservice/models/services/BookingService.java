package org.example.bookingservice.models.services;

import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.constants.BookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    BookingResponse getBookingById(Long bookingId);

    List<BookingResponse> getBookings(String customerEmail);

    BookingResponse updateBookingStatus(Long bookingId, BookingStatus status);

    void cancelBooking(Long bookingId);
}
