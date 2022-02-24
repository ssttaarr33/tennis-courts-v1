package com.tenniscourts.reservations;

import com.tenniscourts.exceptions.AlreadyExistsEntityException;
import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.guests.Guest;
import com.tenniscourts.guests.GuestMapper;
import com.tenniscourts.guests.GuestService;
import com.tenniscourts.schedules.Schedule;
import com.tenniscourts.schedules.ScheduleMapper;
import com.tenniscourts.schedules.ScheduleService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ReservationService {

    private static final BigDecimal RESERVATION_FEE = new BigDecimal(10);
    private final ReservationRepository reservationRepository;
    private final ReservationMapper reservationMapper;
    private final GuestMapper guestMapper;
    private final ScheduleMapper scheduleMapper;
    private final GuestService guestService;
    private final ScheduleService scheduleService;

    public ReservationDTO bookReservation(CreateReservationRequestDTO createReservationRequestDTO) {
        Guest guest = guestMapper.map(guestService.findById(createReservationRequestDTO.getGuestId()));
        Schedule schedule = scheduleMapper.map(
                scheduleService.findSchedule(createReservationRequestDTO.getScheduleId()));
        checkReservationExists(schedule);
        checkValidInterval(schedule);

        return reservationMapper.map(reservationRepository.save(Reservation
                .builder()
                .guest(guest)
                .schedule(schedule)
                .reservationStatus(ReservationStatus.READY_TO_PLAY)
                .value(RESERVATION_FEE)
                .build()));
    }

    public List<ReservationDTO> getReservations(ReservationFilterDTO filterDTO) {
        return reservationMapper.map(
                reservationRepository.findBySchedule_StartDateTimeGreaterThanEqualAndSchedule_EndDateTimeLessThanEqual(
                        filterDTO.getStartDate(), filterDTO.getEndDate()));
    }

    public List<ReservationDTO> getReservations() {
        return reservationMapper.map(
                reservationRepository.findAll());
    }

    public ReservationDTO findReservation(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservationMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    public ReservationDTO cancelReservation(Long reservationId) {
        return reservationMapper.map(this.cancel(reservationId));
    }

    private Reservation cancel(Long reservationId) {
        return reservationRepository.findById(reservationId).map(reservation -> {

            this.validateCancellation(reservation);

            BigDecimal refundValue = getRefundValue(reservation);
            return this.updateReservation(reservation, refundValue);

        }).orElseThrow(() -> {
            throw new EntityNotFoundException("Reservation not found.");
        });
    }

    private Reservation updateReservation(Reservation reservation, BigDecimal refundValue) {
        reservation.setReservationStatus(ReservationStatus.CANCELLED);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        return reservationRepository.save(reservation);
    }

    private void validateCancellation(Reservation reservation) {
        if (!ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus())) {
            throw new IllegalArgumentException("Cannot cancel/reschedule because it's not in ready to play status.");
        }
        checkValidInterval(reservation.getSchedule());
    }

    public BigDecimal getRefundValue(Reservation reservation) {
        long hours = ChronoUnit.HOURS.between(LocalDateTime.now(), reservation.getSchedule().getStartDateTime());

        if (hours >= 24) {
            return reservation.getValue();
        } else if (hours >= 12) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.75));
        } else if (hours >= 2) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.5));
        } else if (hours >= 0) {
            return reservation.getValue().multiply(BigDecimal.valueOf(0.25));
        }
        return BigDecimal.ZERO;
    }

    public ReservationDTO rescheduleReservation(Long previousReservationId, Long scheduleId) {
        Reservation previousReservation = reservationMapper.map(findReservation(previousReservationId));
        if (scheduleId.equals(previousReservation.getSchedule().getId())) {
            throw new IllegalArgumentException("Cannot reschedule to the same slot.");
        }

        reschedule(previousReservation);
        ReservationDTO newReservation = bookReservation(
                CreateReservationRequestDTO.builder()
                        .guestId(previousReservation.getGuest().getId())
                        .scheduleId(scheduleId)
                        .build());
        newReservation.setPreviousReservation(reservationMapper.map(previousReservation));
        return newReservation;
    }

    private void reschedule(Reservation previousReservation) {
        validateCancellation(previousReservation);
        BigDecimal refundValue = getRefundValue(previousReservation);
        update(previousReservation, refundValue);
    }

    private void update(Reservation reservation, BigDecimal refundValue) {
        reservation.setReservationStatus(ReservationStatus.RESCHEDULED);
        reservation.setValue(reservation.getValue().subtract(refundValue));
        reservation.setRefundValue(refundValue);

        reservationRepository.save(reservation);
    }

    private void checkReservationExists(Schedule schedule) {
        List<Reservation> reservations = reservationRepository.findBySchedule_Id(schedule.getId());
        if (reservations.stream()
                .anyMatch(reservation -> ReservationStatus.READY_TO_PLAY.equals(reservation.getReservationStatus()))) {
            throw new AlreadyExistsEntityException(String.format("Reservation for tennis court %s exists for time: %s",
                    schedule.getTennisCourt().getName(),
                    schedule.getStartDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"))));
        }
    }

    private void checkValidInterval(Schedule schedule) {
        if (schedule.getStartDateTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Can cancel/reschedule only future dates.");
        }
    }
}
