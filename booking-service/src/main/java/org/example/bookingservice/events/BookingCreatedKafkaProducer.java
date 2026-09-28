package org.example.bookingservice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes the email payload expected by notify-service on booking-created. */
@Component
public class BookingCreatedKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(BookingCreatedKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String bookingCreatedTopic;

    public BookingCreatedKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            @Value("${booking.kafka.booking-created-topic}") String bookingCreatedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.bookingCreatedTopic = bookingCreatedTopic;
    }

    public void publish(BookingCreatedEvent event) {
        kafkaTemplate.send(bookingCreatedTopic, event.bookingId().toString(), event.customerEmail())
                .whenComplete((result, error) -> {
                    if (error == null) {
                        log.debug("Published booking-created event for booking {}", event.bookingId());
                    } else {
                        log.error("Could not publish booking-created event for booking {}", event.bookingId(), error);
                    }
                });
    }
}
