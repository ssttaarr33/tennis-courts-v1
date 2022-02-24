package com.tenniscourts.reservations;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findBySchedule_Id(Long scheduleId);

    List<Reservation> findBySchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
            LocalDateTime startDateTime, LocalDateTime endDateTime);
}
