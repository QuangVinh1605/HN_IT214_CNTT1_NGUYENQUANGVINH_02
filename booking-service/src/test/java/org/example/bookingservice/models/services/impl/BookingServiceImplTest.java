package org.example.bookingservice.models.services.impl;

import org.example.bookingservice.events.BookingCreatedEvent;
import org.example.bookingservice.exceptions.BookingNotFoundException;
import org.example.bookingservice.exceptions.InvalidBookingStatusTransitionException;
import org.example.bookingservice.models.constants.BookingStatus;
import org.example.bookingservice.models.dto.requests.CreateBookingDetailRequest;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.example.bookingservice.models.entities.Booking;
import org.example.bookingservice.models.entities.BookingDetail;
import org.example.bookingservice.models.repositories.BookingDetailRepository;
import org.example.bookingservice.models.repositories.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingServiceImplTest {

    private BookingRepository bookingRepository;
    private BookingDetailRepository bookingDetailRepository;
    private MovieGatewayService movieGatewayService;
    private ApplicationEventPublisher eventPublisher;
    private BookingServiceImpl bookingService;

    @BeforeEach
    void setUp() {
        bookingRepository = mock(BookingRepository.class);
        bookingDetailRepository = mock(BookingDetailRepository.class);
        movieGatewayService = mock(MovieGatewayService.class);
        eventPublisher = mock(ApplicationEventPublisher.class);
        bookingService = new BookingServiceImpl(
                bookingRepository,
                bookingDetailRepository,
                movieGatewayService,
                eventPublisher);
    }

    @Test
    void createsBookingWithMovieSnapshotsAndPublishesEvent() {
        CreateBookingRequest request = new CreateBookingRequest(
                "  An Nguyen  ",
                "  an@example.com  ",
                List.of(new CreateBookingDetailRequest(1L, 2), new CreateBookingDetailRequest(2L, 3)));
        when(movieGatewayService.getMovieById(1L)).thenReturn(new MovieResponse(1L, "Movie One", 12.50));
        when(movieGatewayService.getMovieById(2L)).thenReturn(new MovieResponse(2L, "Movie Two", 5.00));
        AtomicReference<Booking> savedBooking = new AtomicReference<>();
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            booking.setId(42L);
            booking.getDetails().get(0).setId(101L);
            booking.getDetails().get(1).setId(102L);
            savedBooking.set(booking);
            return booking;
        });
        when(bookingDetailRepository.findAllByBookingIdOrderByIdAsc(42L))
                .thenAnswer(invocation -> savedBooking.get().getDetails());

        BookingResponse response = bookingService.createBooking(request);

        assertEquals(42L, response.id());
        assertEquals("An Nguyen", response.customerName());
        assertEquals("an@example.com", response.customerEmail());
        assertEquals(40.00, response.total());
        assertEquals(BookingStatus.PENDING, response.status());
        assertEquals(2, response.items().size());
        assertEquals("Movie One", response.items().getFirst().movieTitle());
        assertEquals(25.00, response.items().getFirst().subtotal());
        verify(eventPublisher).publishEvent(new BookingCreatedEvent(42L, "an@example.com"));
    }

    @Test
    void doesNotPersistOrPublishWhenAMovieCannotBeResolved() {
        CreateBookingRequest request = new CreateBookingRequest(
                "An", "an@example.com", List.of(new CreateBookingDetailRequest(99L, 1)));
        when(movieGatewayService.getMovieById(99L)).thenThrow(new RuntimeException("Movie service unavailable"));

        assertThrows(RuntimeException.class, () -> bookingService.createBooking(request));

        verify(bookingRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void rejectsAnInvalidStatusTransition() {
        Booking booking = persistedBooking(7L, BookingStatus.PENDING);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));

        assertThrows(InvalidBookingStatusTransitionException.class,
                () -> bookingService.updateBookingStatus(7L, BookingStatus.SUCCESS));

        verify(bookingRepository, never()).save(any());
    }

    @Test
    void returnsNotFoundForUnknownBooking() {
        when(bookingRepository.findById(88L)).thenReturn(Optional.empty());

        assertThrows(BookingNotFoundException.class, () -> bookingService.getBookingById(88L));
    }

    @Test
    void changesPendingBookingToConfirmed() {
        Booking booking = persistedBooking(7L, BookingStatus.PENDING);
        when(bookingRepository.findById(7L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingDetailRepository.findAllByBookingIdOrderByIdAsc(7L)).thenReturn(booking.getDetails());

        BookingResponse response = bookingService.updateBookingStatus(7L, BookingStatus.CONFIRM);

        assertEquals(BookingStatus.CONFIRM, response.status());
        verify(bookingRepository).save(booking);
    }

    private Booking persistedBooking(Long id, BookingStatus status) {
        Booking booking = Booking.builder()
                .id(id)
                .customerName("An")
                .customerEmail("an@example.com")
                .total(12.50)
                .status(status)
                .build();
        booking.addDetail(BookingDetail.builder()
                .id(1L)
                .movieId(1L)
                .movieTitle("Movie One")
                .quantity(1)
                .unitPrice(12.50)
                .build());
        return booking;
    }
}
