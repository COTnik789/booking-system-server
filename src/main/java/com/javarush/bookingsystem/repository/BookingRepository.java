package com.javarush.bookingsystem.repository;


import com.javarush.bookingsystem.domain.Booking;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {


    @Query("""
SELECT b FROM Booking b
WHERE b.room.id = :roomId
AND b.startTime < :endTime
AND b.endTime > :startTime
""")
    List<Booking> findOverlaps(@Param("roomId") Long roomId,
                               @Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
SELECT b FROM Booking b
WHERE b.room.id = :roomId
AND b.startTime < :endTime
AND b.endTime > :startTime
""")
    List<Booking> findOverlapsForUpdate(@Param("roomId") Long roomId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);


    List<Booking> findByRoomIdOrderByStartTime(Long roomId);

    @Query("""
SELECT b FROM Booking b
WHERE (:roomId IS NULL OR b.room.id = :roomId)
AND b.startTime < :to
AND b.endTime > :from
ORDER BY b.startTime
""")
    List<Booking> findInRange(@Param("from") LocalDateTime from,
                              @Param("to") LocalDateTime to,
                              @Param("roomId") Long roomId);
}