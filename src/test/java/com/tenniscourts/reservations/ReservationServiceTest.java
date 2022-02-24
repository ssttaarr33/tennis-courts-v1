package com.tenniscourts.reservations;

import com.tenniscourts.schedules.Schedule;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@SpringBootTest
@RunWith(MockitoJUnitRunner.class)
@ContextConfiguration(classes = ReservationService.class)
public class ReservationServiceTest {

    @InjectMocks
    ReservationService reservationService;

    @Test
    @DisplayName("Refund full value")
    public void getRefundValueFullRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusDays(2);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(
                Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()), new BigDecimal(10));
    }

    @Test
    @DisplayName("Refund 75% value")
    public void getRefundValue75Refund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(13);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(
                        Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()),
                new BigDecimal(10).multiply(BigDecimal.valueOf(0.75)));
    }

    @Test
    @DisplayName("Refund 50% value")
    public void getRefundValue50Refund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(4);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(
                        Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()),
                new BigDecimal(10).multiply(BigDecimal.valueOf(0.5)));
    }

    @Test
    @DisplayName("Refund 25% value")
    public void getRefundValue25Refund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(2);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(
                        Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()),
                new BigDecimal(10).multiply(BigDecimal.valueOf(0.25)));
    }


    @Test
    @DisplayName("No refund")
    public void noRefund() {
        Schedule schedule = new Schedule();

        LocalDateTime startDateTime = LocalDateTime.now().minusHours(1);

        schedule.setStartDateTime(startDateTime);

        Assert.assertEquals(reservationService.getRefundValue(
                        Reservation.builder().schedule(schedule).value(new BigDecimal(10L)).build()),
                new BigDecimal(0));
    }
}