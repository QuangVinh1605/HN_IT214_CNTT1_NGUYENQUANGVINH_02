package org.example.bookingservice.models.dto.requests;

import jakarta.validation.constraints.NotNull;
import org.example.bookingservice.models.constants.BookingStatus;

public record UpdateBookingStatusRequest(
        @NotNull(message = "Booking status is required")
        BookingStatus status
) {
}
