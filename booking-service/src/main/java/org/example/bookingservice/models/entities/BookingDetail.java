package org.example.bookingservice.models.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_details")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookingDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "movie_title", nullable = false)
    private String movieTitle;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;
}
