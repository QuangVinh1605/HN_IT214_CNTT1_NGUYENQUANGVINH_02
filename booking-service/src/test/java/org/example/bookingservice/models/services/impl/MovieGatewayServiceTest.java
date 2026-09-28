package org.example.bookingservice.models.services.impl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.example.bookingservice.clients.MovieClient;
import org.example.bookingservice.exceptions.MovieServiceException;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MovieGatewayServiceTest {

    @Test
    void returnsTheMovieWhenMovieServiceRespondsWithValidData() {
        MovieClient movieClient = mock(MovieClient.class);
        MovieGatewayService gatewayService = new MovieGatewayService(movieClient);
        MovieResponse expected = new MovieResponse(8L, "Dune", 10.00);
        when(movieClient.getMovieById(8L)).thenReturn(expected);

        assertSame(expected, gatewayService.getMovieById(8L));
    }

    @Test
    void exposesCircuitBreakerAndFallbackConfiguration() throws NoSuchMethodException {
        CircuitBreaker circuitBreaker = MovieGatewayService.class
                .getMethod("getMovieById", Long.class)
                .getAnnotation(CircuitBreaker.class);

        assertEquals("movieService", circuitBreaker.name());
        assertEquals("getMovieFallback", circuitBreaker.fallbackMethod());
    }

    @Test
    void convertsUnexpectedMovieServiceFailureToGatewayError() {
        MovieGatewayService gatewayService = new MovieGatewayService(mock(MovieClient.class));

        MovieServiceException exception = assertThrows(MovieServiceException.class,
                () -> gatewayService.getMovieFallback(8L, new RuntimeException("Connection refused")));

        assertEquals("Movie service is unavailable for movie 8", exception.getMessage());
    }
}
