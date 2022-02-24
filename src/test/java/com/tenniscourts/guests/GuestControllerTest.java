package com.tenniscourts.guests;

import static com.tenniscourts.utils.Constants.GUEST_BY_NAME_PATH;
import static com.tenniscourts.utils.Constants.GUEST_ID;
import static com.tenniscourts.utils.Constants.GUEST_NAME;
import static com.tenniscourts.utils.Constants.HOST;
import static com.tenniscourts.utils.Constants.NON_EXISTING_GUEST_NAME;
import static com.tenniscourts.utils.Constants.TEST_GUEST_NAME;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import com.tenniscourts.utils.BaseTestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import org.apache.http.HttpStatus;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;


public class GuestControllerTest extends BaseTestConfig {

    @PostConstruct
    public void init() {
        uri = HOST + port;
        basePath = "/guests";
        RestAssured.defaultParser = Parser.JSON;
    }

    @Test
    @DisplayName("when get all guests then correct size list is returned")
    public void whenGetAllGuestsThenCorrectListIsReturned() {
        given()
                .when()
                .get(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("data.size()", is(2));
    }

    @Test
    @DisplayName("when get guest by id then correct guest is returned")
    public void whenGetGuestByIdThenCorrectGuestIsReturned() {
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", GUEST_ID))
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", is(GUEST_NAME))
                .body("id", is(GUEST_ID));
    }

    @Test
    @DisplayName("when get guest by name then correct guest is returned")
    public void whenGetGuestByNameThenCorrectGuestIsReturned() {
        given()
                .queryParam("name", GUEST_NAME)
                .when()
                .get(uri + basePath + GUEST_BY_NAME_PATH)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", is(GUEST_NAME))
                .body("id", is(GUEST_ID));
    }

    @Test
    @DisplayName("when get guest by non existing name then correct status")
    public void whenGetGuestByNonExistingNameThenCorrectStatus() {
        given()
                .queryParam("name", NON_EXISTING_GUEST_NAME)
                .when()
                .get(uri + basePath + GUEST_BY_NAME_PATH)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @DisplayName("when add guest then correct guest is returned")
    public void whenAddGuestThenCorrectGuestIsReturned() {
        GuestDTO expectedGuest = new GuestDTO();
        expectedGuest.setName(TEST_GUEST_NAME);

        given()
                .contentType(ContentType.JSON)
                .body(expectedGuest)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", is(TEST_GUEST_NAME))
                .body("id", is(3));
    }

    @Test
    @DisplayName("when add guest no name then correct status is returned")
    public void whenAddGuestNoNameThenCorrectStatusIsReturned() {
        GuestDTO expectedGuest = new GuestDTO();

        given()
                .contentType(ContentType.JSON)
                .body(expectedGuest)
                .when()
                .post(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    @Transactional
    @DisplayName("when update guest then correct guest is returned")
    public void whenUpdateGuestThenCorrectGuestIsReturned() {
        GuestDTO expectedGuest = new GuestDTO();
        expectedGuest.setId(1L);
        expectedGuest.setName(TEST_GUEST_NAME);

        given()
                .contentType(ContentType.JSON)
                .body(expectedGuest)
                .when()
                .put(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body("name", is(TEST_GUEST_NAME))
                .body("id", is(GUEST_ID));
    }

    @Test
    @Transactional
    @DisplayName("when update guest wrong id then correct status is returned")
    public void whenUpdateGuestWrongIdThenCorrectStatusIsReturned() {
        GuestDTO expectedGuest = new GuestDTO();
        expectedGuest.setId(10L);
        expectedGuest.setName(TEST_GUEST_NAME);

        given()
                .contentType(ContentType.JSON)
                .body(expectedGuest)
                .when()
                .put(uri + basePath)
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Transactional
    @DisplayName("when delete guest then correct status is returned")
    public void whenDeleteGuestThenCorrectStatusIsReturned() {
        given()
                .when()
                .delete(String.format(uri + basePath + "/%s", 2))
                .then()
                .statusCode(HttpStatus.SC_OK);
        given()
                .when()
                .get(String.format(uri + basePath + "/%s", 2))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test
    @Transactional
    @DisplayName("when delete guest with wrong id then correct status is returned")
    public void whenDeleteGuestWithWrongIdThenCorrectStatusIsReturned() {
        given()
                .when()
                .delete(String.format(uri + basePath + "/%s", 10))
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
