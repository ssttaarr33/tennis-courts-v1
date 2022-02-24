package com.tenniscourts.reservations;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/reservations")
public class ReservationController extends BaseRestController {

    private final ReservationService reservationService;

    @PostMapping("/filter")
    @ApiOperation(value = "Get reservations for interval")
    public ResponseEntity<List<ReservationDTO>> getReservations(
            @RequestBody ReservationFilterDTO reservationFilterDTO) {
        return ResponseEntity.ok(reservationService.getReservations(reservationFilterDTO));
    }

    @PostMapping
    @ApiOperation(value = "Book a reservation for a guest")
    public ResponseEntity<Void> bookReservation(@RequestBody CreateReservationRequestDTO createReservationRequestDTO) {
        return ResponseEntity.created(
                locationByEntity(reservationService.bookReservation(createReservationRequestDTO).getId())).build();
    }

    @GetMapping("/{reservationId}")
    @ApiOperation(value = "Find reservation by ID")
    public ResponseEntity<ReservationDTO> findReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.findReservation(reservationId));
    }


    @GetMapping
    @ApiOperation(value = "Get all reservations")
    public ResponseEntity<List<ReservationDTO>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getReservations());
    }

    @DeleteMapping("/{reservationId}")
    @ApiOperation(value = "Cancel a reservation")
    public ResponseEntity<ReservationDTO> cancelReservation(@PathVariable Long reservationId) {
        return ResponseEntity.ok(reservationService.cancelReservation(reservationId));
    }

    @PutMapping("/{reservationId}/{scheduleId}")
    @ApiOperation(value = "Reschedule a reservation")
    public ResponseEntity<ReservationDTO> rescheduleReservation(@PathVariable Long reservationId,
            @PathVariable Long scheduleId) {
        return ResponseEntity.ok(reservationService.rescheduleReservation(reservationId, scheduleId));
    }
}
