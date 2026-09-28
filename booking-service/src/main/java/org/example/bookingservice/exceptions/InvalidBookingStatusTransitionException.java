package org.example.bookingservice.exceptions;

import org.example.bookingservice.models.constants.BookingStatus;

public class InvalidBookingStatusTransitionException extends RuntimeException {

    public InvalidBookingStatusTransitionException(BookingStatus current, BookingStatus requested) {
        super("Cannot change booking status from " + current + " to " + requested);
    }
}
