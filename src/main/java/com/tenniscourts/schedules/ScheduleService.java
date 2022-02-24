package com.tenniscourts.schedules;

import static java.util.Objects.isNull;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.reservations.ReservationStatus;
import com.tenniscourts.tenniscourts.TennisCourtDTO;
import com.tenniscourts.tenniscourts.TennisCourtMapper;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    private final ScheduleMapper scheduleMapper;

    private final TennisCourtMapper tennisCourtMapper;

    private final TennisCourtRepository tennisCourtRepository;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        if (isNull(createScheduleRequestDTO.getStartDateTime())) {
            throw new IllegalArgumentException("Start date and time is missing.");
        }
        if (createScheduleRequestDTO.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot add schedule for past dates.");
        }
        checkAlreadyScheduledSlot(tennisCourtId, createScheduleRequestDTO);
        return scheduleMapper.map(scheduleRepository.save(scheduleNewSlot(tennisCourtId, createScheduleRequestDTO)));
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(
                scheduleRepository.findAllByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate,
                        endDate));
    }

    public List<ScheduleDTO> findAllSchedules() {
        return scheduleRepository.findAll().stream().map(scheduleMapper::map).collect(Collectors.toList());
    }

    public List<ScheduleDTO> findAvailableSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(
                scheduleRepository.findAllByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate,
                        endDate).stream().filter(availableScheduleFilter()).collect(Collectors.toList()));
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).map(scheduleMapper::map)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException(String.format("Schedule not found for id %s", scheduleId));
                });
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }

    private void checkAlreadyScheduledSlot(Long tennisCourt, CreateScheduleRequestDTO createScheduleRequestDTO) {
        if (scheduleRepository.findByTennisCourt_IdAndStartDateTime(tennisCourt,
                createScheduleRequestDTO.getStartDateTime()).isPresent()) {
            throw new AlreadyExistsEntityException("Slot is already scheduled for given interval");
        }
    }

    private Schedule scheduleNewSlot(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        TennisCourtDTO tennisCourtDTO = tennisCourtRepository.findById(tennisCourtId)
                .map(tennisCourtMapper::map)
                .orElseThrow(() -> {
                    throw new EntityNotFoundException(String.format("Tennis court not found for id %s", tennisCourtId));
                });
        return Schedule.builder()
                .tennisCourt(tennisCourtMapper.map(tennisCourtDTO))
                .startDateTime(createScheduleRequestDTO.getStartDateTime())
                .endDateTime(createScheduleRequestDTO.getStartDateTime().plusHours(1L))
                .build();
    }

    private Predicate<Schedule> availableScheduleFilter() {
        return schedule -> {
            boolean scheduleBooked = schedule.getReservations().stream()
                    .anyMatch(
                            reservation -> ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus()));
            return !scheduleBooked;
        };
    }
}
