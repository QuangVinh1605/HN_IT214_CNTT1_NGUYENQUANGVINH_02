package org.example.bookingservice.events;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingCreatedKafkaProducerTest {

    @Test
    void sendsCustomerEmailToTheConfiguredTopic() {
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        when(kafkaTemplate.send(eq("booking-created"), eq("12"), eq("customer@example.com")))
                .thenReturn(CompletableFuture.<SendResult<String, String>>completedFuture(null));
        BookingCreatedKafkaProducer producer = new BookingCreatedKafkaProducer(kafkaTemplate, "booking-created");

        producer.publish(new BookingCreatedEvent(12L, "customer@example.com"));

        verify(kafkaTemplate).send("booking-created", "12", "customer@example.com");
    }
}
