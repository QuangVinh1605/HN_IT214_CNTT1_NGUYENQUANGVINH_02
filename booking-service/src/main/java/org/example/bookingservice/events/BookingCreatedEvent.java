package org.example.bookingservice.events;

/**
 * Internal event emitted once a new booking has been persisted.  It is handled
 * after the database transaction commits so notifications are never sent for a
 * booking that subsequently rolls back.
 */
public record BookingCreatedEvent(Long bookingId, String customerEmail) {
}
