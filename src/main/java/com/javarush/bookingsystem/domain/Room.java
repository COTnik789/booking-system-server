package com.javarush.bookingsystem.domain;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.time.LocalTime;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms", indexes = {
        @Index(name = "ux_room_name", columnList = "name", unique = true)
})
@ToString(exclude = {"id"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Room {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;


    @NotBlank
    @Column(nullable = false, unique = true)
    private String name;


    @NotNull
    @Column(nullable = false)
    private LocalTime openTime;


    @NotNull
    @Column(nullable = false)
    private LocalTime closeTime;
}