package com.javarush.bookingsystem.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "bookings",
        indexes = {
                @Index(name = "idx_booking_room_start", columnList = "room_id,start_time"),
                @Index(name = "idx_booking_room_end", columnList = "room_id,end_time")
        })
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"id", "version", "room"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Booking {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_booking_room"))
    private Room room;


    @NotBlank
    @Column(nullable = false)
    private String userName;


    @NotNull @Future
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;


    @NotNull @Future
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;


    @Version
    private Long version;


    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}