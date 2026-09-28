package org.example.bookingservice.exceptions;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long bookingId) {
        super("Booking not found with id: " + bookingId);
    }
}
