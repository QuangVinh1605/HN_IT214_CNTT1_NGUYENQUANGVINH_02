package org.example.bookingservice.models.services.impl;

import lombok.RequiredArgsConstructor;
import org.example.bookingservice.events.BookingCreatedEvent;
import org.example.bookingservice.models.constants.BookingStatus;
import org.example.bookingservice.models.dto.requests.CreateBookingDetailRequest;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.responses.BookingDetailResponse;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.dto.responses.MovieResponse;
import org.example.bookingservice.models.entities.Booking;
import org.example.bookingservice.models.entities.BookingDetail;
import org.example.bookingservice.models.repositories.BookingDetailRepository;
import org.example.bookingservice.models.repositories.BookingRepository;
import org.example.bookingservice.models.services.BookingService;
import org.example.bookingservice.exceptions.BookingNotFoundException;
import org.example.bookingservice.exceptions.InvalidBookingStatusTransitionException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

        private final BookingRepository bookingRepository;
        private final BookingDetailRepository bookingDetailRepository;
        private final MovieGatewayService movieGatewayService;
        private final ApplicationEventPublisher eventPublisher;

        @Override
        @Transactional
        public BookingResponse createBooking(CreateBookingRequest request) {
                List<ResolvedItem> resolvedItems = request.items().stream()
                                .map(this::resolveMovie)
                                .toList();

                double total = resolvedItems.stream()
                                .map(item -> BigDecimal.valueOf(item.movie().ticketPrice())
                                                .multiply(BigDecimal.valueOf(item.request().quantity())))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP)
                                .doubleValue();

                Booking booking = Booking.builder()
                                .customerName(request.customerName().trim())
                                .customerEmail(request.customerEmail().trim())
                                .total(total)
                                .status(BookingStatus.PENDING)
                                .build();

                resolvedItems.forEach(item -> booking.addDetail(BookingDetail.builder()
                                .movieId(item.movie().id())
                                .movieTitle(item.movie().title().trim())
                                .quantity(item.request().quantity())
                                .unitPrice(item.movie().ticketPrice())
                                .build()));

                Booking savedBooking = bookingRepository.save(booking);
                BookingResponse response = toResponse(savedBooking);

                eventPublisher.publishEvent(new BookingCreatedEvent(
                                savedBooking.getId(),
                                savedBooking.getCustomerEmail()));

                return response;
        }

        @Override
        @Transactional(readOnly = true)
        public BookingResponse getBookingById(Long bookingId) {
                return toResponse(getBooking(bookingId));
        }

        @Override
        @Transactional(readOnly = true)
        public List<BookingResponse> getBookings(String customerEmail) {
                List<Booking> bookings = customerEmail == null || customerEmail.isBlank()
                                ? bookingRepository.findAllByOrderByIdDesc()
                                : bookingRepository.findAllByCustomerEmailIgnoreCaseOrderByIdDesc(customerEmail.trim());

                return bookings.stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Override
        @Transactional
        public BookingResponse updateBookingStatus(Long bookingId, BookingStatus status) {
                Objects.requireNonNull(status, "Booking status is required");
                Booking booking = getBooking(bookingId);
                BookingStatus currentStatus = booking.getStatus();

                if (currentStatus != status && !canTransition(currentStatus, status)) {
                        throw new InvalidBookingStatusTransitionException(currentStatus, status);
                }

                booking.setStatus(status);
                return toResponse(bookingRepository.save(booking));
        }

        @Override
        @Transactional
        public void cancelBooking(Long bookingId) {
                updateBookingStatus(bookingId, BookingStatus.CANCELED);
        }

        private Booking getBooking(Long bookingId) {
                return bookingRepository.findById(bookingId)
                                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        }

        private BookingResponse toResponse(Booking booking) {
                List<BookingDetailResponse> items = bookingDetailRepository
                                .findAllByBookingIdOrderByIdAsc(booking.getId())
                                .stream()
                                .map(this::toDetailResponse)
                                .toList();

                return new BookingResponse(
                                booking.getId(),
                                booking.getCustomerName(),
                                booking.getCustomerEmail(),
                                booking.getTotal(),
                                booking.getStatus(),
                                items);
        }

        private BookingDetailResponse toDetailResponse(BookingDetail detail) {
                return new BookingDetailResponse(
                                detail.getId(),
                                detail.getMovieId(),
                                detail.getMovieTitle(),
                                detail.getQuantity(),
                                detail.getUnitPrice(),
                                BigDecimal.valueOf(detail.getUnitPrice())
                                                .multiply(BigDecimal.valueOf(detail.getQuantity()))
                                                .setScale(2, RoundingMode.HALF_UP)
                                                .doubleValue());
        }

        private ResolvedItem resolveMovie(CreateBookingDetailRequest item) {
                MovieResponse movie = movieGatewayService.getMovieById(item.movieId());
                return new ResolvedItem(item, movie);
        }

        private boolean canTransition(BookingStatus currentStatus, BookingStatus requestedStatus) {
                return switch (currentStatus) {
                        case PENDING -> requestedStatus == BookingStatus.CONFIRM
                                        || requestedStatus == BookingStatus.CANCELED;
                        case CONFIRM -> requestedStatus == BookingStatus.DELIVERED
                                        || requestedStatus == BookingStatus.CANCELED;
                        case DELIVERED -> requestedStatus == BookingStatus.SUCCESS;
                        case SUCCESS, CANCELED -> false;
                };
        }

        private record ResolvedItem(CreateBookingDetailRequest request, MovieResponse movie) {
        }
}
