package org.example.bookingservice.models.services.impl;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.clients.MovieClient;
import org.example.bookingservice.exceptions.MovieNotFoundException;
import org.example.bookingservice.exceptions.MovieServiceException;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MovieGatewayService {

    private final MovieClient movieClient;

    @CircuitBreaker(name = "movieService", fallbackMethod = "getMovieFallback")
    public MovieResponse getMovieById(Long movieId) {
        MovieResponse movie = movieClient.getMovieById(movieId);

        if (movie == null || movie.id() == null || movie.ticketPrice() == null || !StringUtils.hasText(movie.title())) {
            throw new MovieServiceException(
                    "Movie service returned invalid data for movie " + movieId
            );
        }
        if (!movieId.equals(movie.id())) {
            throw new MovieServiceException(
                    "Movie service returned a different movie for movie " + movieId
            );
        }
        if (!Double.isFinite(movie.ticketPrice()) || movie.ticketPrice() < 0) {
            throw new MovieServiceException(
                    "Movie ticket price must be a non-negative finite value for movie " + movieId
            );
        }

        return movie;
    }

    public MovieResponse getMovieFallback(Long movieId, Throwable throwable) {
        if (throwable instanceof FeignException.NotFound) {
            throw new MovieNotFoundException(movieId);
        }
        if (throwable instanceof MovieNotFoundException movieNotFoundException) {
            throw movieNotFoundException;
        }
        if (throwable instanceof MovieServiceException movieServiceException) {
            throw movieServiceException;
        }

        throw new MovieServiceException(
                "Movie service is unavailable for movie " + movieId,
                throwable
        );
    }
}
