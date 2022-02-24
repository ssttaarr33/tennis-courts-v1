package com.tenniscourts.reservations;

import static com.tenniscourts.reservations.ReservationStatus.READY_TO_PLAY;
import static com.tenniscourts.utils.Constants.GUEST_NAME;
import static com.tenniscourts.utils.Constants.HOST;
import static com.tenniscourts.utils.Constants.RESERVATION_ID;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.tenniscourts.utils.BaseTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.apache.http.HttpStatus;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runners.MethodSorters;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ReservationControllerTest extends BaseTestConfig {

    @PostConstruct
    public void init() {
        uri = HOST + port;
        basePath = "/reservations";
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    @DisplayName("when get all reservations then correct size list is returned")
    public void whenGetAllReservationsThenCorrectListIsReturned() {
        given()
                .when()
                .get(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(4));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @DisplayName("when get reservation by id then correct reservation is returned")
    public void whenGetReservationByIdThenCorrectReservationIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", RESERVATION_ID))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(RESERVATION_ID))
                .body("guest.name", is(GUEST_NAME))
                .body("reservationStatus", is(READY_TO_PLAY.toString()));
    }

    @Test
    @DisplayName("when get reservation by non existing id then correct status is returned")
    public void whenGetReservationByNonExistingIdThenCorrectStatusIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 10))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when get all reservations with filters in past then correct size list is returned")
    public void whenGetAllReservationsWithFiltersInPastThenCorrectListIsReturned() {
        ReservationFilterDTO reservationFilterDTO = new ReservationFilterDTO();
        reservationFilterDTO.setStartDate(LocalDateTime.now().minusYears(10));
        reservationFilterDTO.setEndDate(LocalDateTime.now());

        given()
                .contentType(ContentType.JSON)
                .body(reservationFilterDTO)
                .when()
                .post(uri + basePath + "/filter")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(0));
    }

    @Test
    @DisplayName("when get all reservations with filters then correct size list is returned")
    public void whenGetAllReservationsWithFiltersThenCorrectListIsReturned() {
        ReservationFilterDTO reservationFilterDTO = new ReservationFilterDTO();
        reservationFilterDTO.setStartDate(LocalDateTime.now().minusYears(10));
        reservationFilterDTO.setEndDate(LocalDateTime.now().plusYears(10));

        given()
                .contentType(ContentType.JSON)
                .body(reservationFilterDTO)
                .when()
                .post(uri + basePath + "/filter")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(4));
    }

    @Test
    @DisplayName("when book reservation than it is booked")
    public void whenBookReservationThanItIsBooked() {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(1L);

        given()
                .contentType(ContentType.JSON)
                .body(createReservationRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DisplayName("when book reservation with wrong guest than it is not booked")
    public void whenBookReservationWithWrongGuestThanItIsNotBooked() {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(10L);
        createReservationRequestDTO.setScheduleId(1L);

        given()
                .contentType(ContentType.JSON)
                .body(createReservationRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when book reservation with wrong schedule than it is not booked")
    public void whenBookReservationWithWrongScheduleThanItIsNotBooked() {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(10L);

        given()
                .contentType(ContentType.JSON)
                .body(createReservationRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when book reservation with existing schedule than it is not booked")
    public void whenBookReservationWithExistingScheduleThanItIsNotBooked() {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(2L);

        given()
                .contentType(ContentType.JSON)
                .body(createReservationRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("when book reservation with past schedule than it is not booked")
    public void whenBookReservationWithPastScheduleThanItIsNotBooked() {
        CreateReservationRequestDTO createReservationRequestDTO = new CreateReservationRequestDTO();
        createReservationRequestDTO.setGuestId(1L);
        createReservationRequestDTO.setScheduleId(3L);

        given()
                .contentType(ContentType.JSON)
                .body(createReservationRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @Transactional
    @DisplayName("when cancel reservation then it is canceled")
    public void whenCancelReservationThenItIsCanceled() {
        given()
                .when()
                .delete(String.format(uri + basePath + "/%s", 1))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(1));
    }

    @Test
    @Transactional
    @DisplayName("when cancel reservation wth wrong status then it is not canceled")
    public void whenCancelReservationWithWrongStatusThenItIsCanceled() {
        given()
                .when()
                .delete(String.format(uri + basePath + "/%s", 2))
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @Transactional
    @DisplayName("when cancel reservation wth wrong id then it is not canceled")
    public void whenCancelReservationWithWrongIdThenItIsCanceled() {
        given()
                .when()
                .delete(String.format(uri + basePath + "/%s", 10))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.BEFORE_METHOD)
    @Transactional
    @DisplayName("when reschedule reservation then it is rescheduled")
    public void whenRescheduleReservationThenItIsRescheduled() {
        given()
                .when()
                .put(String.format(uri + basePath + "/%s/%s", 1, 1))
                .then()
                .statusCode(HttpStatus.SC_OK);
    }

    @Test
    @Transactional
    @DisplayName("when reschedule reservation for same slot then it is not rescheduled")
    public void whenRescheduleReservationForSameSlotThenItIsNotRescheduled() {
        given()
                .when()
                .put(String.format(uri + basePath + "/%s/%s", 1, 2))
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
