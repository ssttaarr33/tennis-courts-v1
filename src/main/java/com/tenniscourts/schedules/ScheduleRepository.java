package com.tenniscourts.schedules;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByTennisCourt_IdOrderByStartDateTime(Long id);

    Optional<Schedule> findByTennisCourt_IdAndStartDateTime(Long id, LocalDateTime localDateTime);

    List<Schedule> findAllByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(LocalDateTime startDateTime,
            LocalDateTime endDateTime);
}