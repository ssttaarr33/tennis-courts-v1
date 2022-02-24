package com.tenniscourts.schedules;

import com.tenniscourts.config.BaseRestController;
import io.swagger.annotations.ApiOperation;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("schedules")
public class ScheduleController extends BaseRestController {

    private final ScheduleService scheduleService;

    @PostMapping
    @ApiOperation(value = "Add a new schedule slot for a given tennis court")
    public ResponseEntity<Void> addScheduleTennisCourt(@RequestBody CreateScheduleRequestDTO createScheduleRequestDTO) {
        return ResponseEntity.created(locationByEntity(
                scheduleService.addSchedule(createScheduleRequestDTO.getTennisCourtId(), createScheduleRequestDTO)
                        .getId())).build();
    }

    @PostMapping("/filter")
    @ApiOperation(value = "Find schedules in interval")
    public ResponseEntity<List<ScheduleDTO>> findSchedulesByDates(@RequestBody ScheduleFilterDTO scheduleFilterDTO) {
        return ResponseEntity.ok(scheduleService.findSchedulesByDates(scheduleFilterDTO.getStartDate(),
                scheduleFilterDTO.getEndDate()));
    }

    @GetMapping("/{scheduleId}")
    @ApiOperation(value = "Find a schedule by ID")
    public ResponseEntity<ScheduleDTO> findByScheduleId(@PathVariable Long scheduleId) {
        return ResponseEntity.ok(scheduleService.findSchedule(scheduleId));
    }

    @GetMapping
    @ApiOperation(value = "Find all schedules")
    public ResponseEntity<List<ScheduleDTO>> findAllSchedules() {
        return ResponseEntity.ok(scheduleService.findAllSchedules());
    }

    @PostMapping("/filter/available")
    @ApiOperation(value = "Find available schedules")
    public ResponseEntity<List<ScheduleDTO>> findAvailableSchedules(@RequestBody ScheduleFilterDTO scheduleFilterDTO) {
        return ResponseEntity.ok(scheduleService.findAvailableSchedulesByDates(scheduleFilterDTO.getStartDate(),
                scheduleFilterDTO.getEndDate()));
    }
}
