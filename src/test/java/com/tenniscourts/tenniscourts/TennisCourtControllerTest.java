package com.tenniscourts.tenniscourts;

import static com.tenniscourts.utils.Constants.HOST;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.tenniscourts.utils.BaseTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import javax.annotation.PostConstruct;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;

public class TennisCourtControllerTest extends BaseTestConfig {

    @PostConstruct
    public void init() {
        uri = HOST + port;
        basePath = "/tennis-courts";
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    @DisplayName("when get tennis court by id then correct tennis court is returned")
    public void whenGetTennisCourtByIdThenCorrectTennisCourtIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 1))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(1))
                .body("name", is("Roland Garros - Court Philippe-Chatrier"));
    }

    @Test
    @DisplayName("when get tennis court by missing id then tennis court is not returned")
    public void whenGetTennisCourtByMissingIdThenTennisCourtIsNotReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 10))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when get tennis court with schedules by id then correct tennis court is returned")
    public void whenGetTennisCourtWithSchedulesByIdThenCorrectTennisCourtIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s/schedules", 1)).prettyPeek()
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("id", is(1))
                .body("name", is("Roland Garros - Court Philippe-Chatrier"))
                .body("tennisCourtSchedules.size()", is(3));
    }

    @Test
    @DisplayName("when get tennis court with schedules by missing id then tennis court is not returned")
    public void whenGetTennisCourtWithSchedulesByMissingIdThenTennisCourtIsNotReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s/schedules", 10))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when add tennis court with bad request then correct status is returned")
    public void whenGetTennisCourtWithBadRequestThenCorrectStatusIsReturned() {
        given()
                .contentType(ContentType.JSON)
                .body(new TennisCourtDTO())
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @DisplayName("when add tennis court then tennis court is created")
    public void whenAddTennisCourtThenTennisCourtIsCreated() {
        TennisCourtDTO tennisCourtDTO = TennisCourtDTO.builder()
                .name("Ghencea Tennis Resort")
                .build();
        given()
                .contentType(ContentType.JSON)
                .body(tennisCourtDTO)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_CREATED);
    }
}
