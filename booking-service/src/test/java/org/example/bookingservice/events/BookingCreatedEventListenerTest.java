package org.example.bookingservice.events;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class BookingCreatedEventListenerTest {

    @Test
    void delegatesCommittedEventToKafkaProducer() {
        BookingCreatedKafkaProducer producer = mock(BookingCreatedKafkaProducer.class);
        BookingCreatedEventListener listener = new BookingCreatedEventListener(producer);
        BookingCreatedEvent event = new BookingCreatedEvent(12L, "customer@example.com");

        listener.publishBookingCreated(event);

        verify(producer).publish(event);
    }
}
