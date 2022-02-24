package com.tenniscourts.schedules;

import static com.tenniscourts.utils.Constants.HOST;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.tenniscourts.utils.BaseTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.time.LocalDateTime;
import javax.annotation.PostConstruct;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.MethodMode;

public class ScheduleControllerTest extends BaseTestConfig {

    @PostConstruct
    public void init() {
        uri = HOST + port;
        basePath = "/schedules";
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    @DisplayName("when get all schedules then correct size list is returned")
    public void whenGetAllSchedulesThenCorrectListIsReturned() {
        given()
                .when()
                .get(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(3));
    }

    @Test
    @DisplayName("when get schedule by id then correct schedule is returned")
    public void whenGetScheduleByIdThenCorrectScheduleIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 1))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(1))
                .body("tennisCourt.id", is(1))
                .body("tennisCourt.name", is("Roland Garros - Court Philippe-Chatrier"));
    }

    @Test
    @DisplayName("when get schedule by missing id then correct status is returned")
    public void whenGetScheduleByMissingIdThenCorrectStatusIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 10)).prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when get all schedules in interval then correct size list is returned")
    public void whenGetAllSchedulesInIntervalThenCorrectListIsReturned() {
        ScheduleFilterDTO scheduleFilterDTO = new ScheduleFilterDTO();
        scheduleFilterDTO.setStartDate(LocalDateTime.now().minusYears(10));
        scheduleFilterDTO.setEndDate(LocalDateTime.now().plusYears(10));

        given()
                .contentType(ContentType.JSON)
                .body(scheduleFilterDTO)
                .when()
                .post(uri + basePath + "/filter")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(3));
    }

    @Test
    @DisplayName("when get all available schedules then correct size list is returned")
    public void whenGetAllAvailableSchedulesThenCorrectListIsReturned() {
        ScheduleFilterDTO scheduleFilterDTO = new ScheduleFilterDTO();
        scheduleFilterDTO.setStartDate(LocalDateTime.now());
        scheduleFilterDTO.setEndDate(LocalDateTime.now().plusYears(10));

        given()
                .contentType(ContentType.JSON)
                .body(scheduleFilterDTO)
                .when()
                .post(uri + basePath + "/filter/available")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(2));
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("when schedule tennis court then schedule is created")
    public void whenScheduleTennisCourtThenScheduleIsCreated() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().plusYears(1));
        createScheduleRequestDTO.setTennisCourtId(1L);
        given()
                .contentType(ContentType.JSON)
                .body(createScheduleRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("when schedule tennis court in the past then schedule is not created")
    public void whenScheduleTennisCourtInThePastThenScheduleIsNotCreated() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now());
        createScheduleRequestDTO.setTennisCourtId(1L);
        given()
                .contentType(ContentType.JSON)
                .body(createScheduleRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DirtiesContext(methodMode = MethodMode.AFTER_METHOD)
    @DisplayName("when schedule tennis court at existing date then schedule is not created")
    public void whenScheduleTennisCourtAtExistingDateThenScheduleIsNotCreated() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.parse("2023-12-20T20:00:00.0"));
        createScheduleRequestDTO.setTennisCourtId(1L);
        given()
                .contentType(ContentType.JSON)
                .body(createScheduleRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_CONFLICT);
    }

    @Test
    @DisplayName("when schedule tennis court non existent then schedule is not created")
    public void whenScheduleTennisCourtNonExistentThenScheduleIsNotCreated() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setStartDateTime(LocalDateTime.now().plusYears(1));
        createScheduleRequestDTO.setTennisCourtId(12L);
        given()
                .contentType(ContentType.JSON)
                .body(createScheduleRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when schedule tennis court with missing start date then schedule is not created")
    public void whenScheduleTennisCourtWithMissingStartDateThenScheduleIsNotCreated() {
        CreateScheduleRequestDTO createScheduleRequestDTO = new CreateScheduleRequestDTO();
        createScheduleRequestDTO.setTennisCourtId(1L);
        given()
                .contentType(ContentType.JSON)
                .body(createScheduleRequestDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }
}
