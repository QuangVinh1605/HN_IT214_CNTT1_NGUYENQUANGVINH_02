package org.example.bookingservice.events;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookingCreatedEventListener {

    private final BookingCreatedKafkaProducer producer;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishBookingCreated(BookingCreatedEvent event) {
        producer.publish(event);
    }
}
