package com.optazen.skillmatch;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.io.File;

import static io.restassured.RestAssured.given;

@QuarkusTest
class ApiResourceTest {
    @Test
    void testEndpoints() {
        given()
                .when()
                .contentType("application/json")
                .post("/api/solve")
                .then()
                .statusCode(204);

        given()
                .when()
                .contentType("application/json")
                .get("/api/read")
                .then()
                .statusCode(200);

        given()
                .body(new File("src/main/resources/data.json"))
                .contentType("application/json")
                .when()
                .post("/api/update")
                .then()
                .statusCode(200);

        given()
                .when()
                .contentType("application/json")
                .post("/api/reset")
                .then()
                .statusCode(200);

    }

}