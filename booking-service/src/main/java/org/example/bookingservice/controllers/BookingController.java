package org.example.bookingservice.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.example.bookingservice.models.dto.requests.CreateBookingRequest;
import org.example.bookingservice.models.dto.requests.UpdateBookingStatusRequest;
import org.example.bookingservice.models.dto.responses.BookingResponse;
import org.example.bookingservice.models.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping
    public List<BookingResponse> getBookings(@RequestParam(required = false) String customerEmail) {
        return bookingService.getBookings(customerEmail);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBookingById(@PathVariable @Positive Long bookingId) {
        return bookingService.getBookingById(bookingId);
    }

    @PatchMapping("/{bookingId}/status")
    public BookingResponse updateStatus(
            @PathVariable @Positive Long bookingId,
            @Valid @RequestBody UpdateBookingStatusRequest request) {
        return bookingService.updateBookingStatus(bookingId, request.status());
    }

    @DeleteMapping("/{bookingId}")
    public ResponseEntity<Void> cancelBooking(@PathVariable @Positive Long bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
}

