package org.example.bookingservice.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.example.bookingservice.models.constants.BookingStatus;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "total", nullable = false)
    private Double total;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    private List<BookingDetail> details = new ArrayList<>();

    public void addDetail(BookingDetail detail) {
        details.add(detail);
        detail.setBooking(this);
    }
}
