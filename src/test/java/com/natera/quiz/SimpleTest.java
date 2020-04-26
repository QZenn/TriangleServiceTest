package com.natera.quiz;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import matchers.isUUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

public class SimpleTest {

    private static RequestSpecification spec;
    private static Logger LOGGER = null;

    @BeforeClass
    public void configure() {
        String token = "4a2cb0d2-058e-43aa-a031-0431dce58095";
        spec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri("https://qa-quiz.natera.com/")
                .addHeader("X-User", token)
                .addFilter(new ResponseLoggingFilter())//log request and response for better debugging. You can also only log if a requests fails.
                .addFilter(new RequestLoggingFilter())
                .build();

        System.setProperty("log4j.configurationFile", "log4j2.xml");
        LOGGER = LogManager.getLogger(this.getClass().getName());
        LOGGER.info("Configuration completed. Start Test Run...");
    }

    private RequestSpecification request() {
        return RestAssured.given().spec(spec);
    }

    private double calcArea(double a, double b, double c) {
        double p = 0.5 * (a + b + c);
        System.out.println(p);
        double result = Math.sqrt(p * (p - a) * (p - b) * (p - c));
        return result;
    }

    @Test
    public void createTriangle() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");

        Response response = request()
                .body(requestParams.toJSONString())
                .post("/triangle");
        LOGGER.info("Create Triangle" + response.asString());
        response.then().assertThat()
                .statusCode(200)
                .body("id", isUUID.isUUID())
                .body("firstSide", equalTo(3.0F))
                .body("secondSide", equalTo(4.0F))
                .body("thirdSide", equalTo(5.0F));

    }

    @Test
    public void getAllTriangles() {
        createTriangle();
        Response response = request().get("/triangle/" + "all");
        LOGGER.info("All Triangles => " + response.asString());
        Assert.assertEquals(response.statusCode(), 200);
        response.getBody();
        response.then()
                .assertThat()
                .body(notNullValue())
                .body("[0].id", isUUID.isUUID())
                .body("[0].firstSide", notNullValue())
                .body("[0].secondSide", notNullValue())
                .body("[0].thirdSide", notNullValue());
    }

    @Test
    public void deleteTriangle() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");

        Response createTriangle = request()
                .body(requestParams.toJSONString())
                .post("/triangle");
        LOGGER.info("Create Triangle => " + createTriangle.asString());
        createTriangle.then().assertThat().statusCode(200);

        String triangleId = createTriangle.jsonPath().get("id");

        Response getTriangle = request().get("/triangle/" + triangleId);
        LOGGER.info("Get Triangle => " + getTriangle.asString());
        getTriangle.then().assertThat().statusCode(200);

        Response deleteTriangle = request().delete("/triangle/" + triangleId);
        LOGGER.info("Delete Triangle = > " + deleteTriangle.asString());
        deleteTriangle.then().assertThat().statusCode(200);

        Response checkDeletion = request().get("/triangle/" + triangleId);
        LOGGER.info("Check Deletion => " + checkDeletion.asString());
        checkDeletion.then().assertThat().statusCode(404);
    }

    @Test
    public void getPerimeter() {
        Response triangle = request()
                .body("{ \"input\" : \"3;4;5\"}")
                .post("/triangle");
        triangle
                .then()
                .assertThat().statusCode(200);

        String triangleId = triangle.jsonPath().get("id");

        Response perimeter = request().get("/triangle/" + triangleId + "/perimeter");
        LOGGER.info("Perimeter => " + perimeter.asString());
        perimeter.then().assertThat()
                .statusCode(200)
                .body("result", equalTo(12.0F));
    }

    @Test
    public void getArea() {
        float expectedArea = (float)calcArea(3.0, 4.0, 5.0);
        String triangleId = createTriangle(";", "3;4;5").jsonPath().get("id");

        Response area = request().get("/triangle/" + triangleId + "/area");
        LOGGER.info("Area => " + area.asString());
        area.then().assertThat()
                .statusCode(200)
                .body("result", equalTo(expectedArea));
    }

    @Test
    private void deleteAllTriangles() {
        reachLimit();
        clean();
    }


    @AfterMethod
    public void clean() {
        Response getAllTriangles = request().get("/triangle/" + "all");
        LOGGER.info(" Get All Triangles => " + getAllTriangles.asString());
        getAllTriangles.then().assertThat().statusCode(200);

        JsonPath triangles = getAllTriangles.then().extract().body().jsonPath();
        List<Map> triangleList = triangles.getList(".");
        for (Map item : triangleList) {
            Response response = request().delete("/triangle/" + item.get("id"));
            LOGGER.info("Delete => " + response.asString());
            response.then().assertThat().statusCode(200);
        }
    }

    @Test
    public void reachLimit() {
        int attempt = 0;
        Response response = null;
        while (attempt < 20) {
            attempt++;
            LOGGER.info("Create Triangle => " + attempt);
            response = createTriangle("1;1;1");
            if (response.getStatusCode() != 200) {
                break;
            }
        }
        response.then().assertThat()
                .statusCode(422)
        .body("status", equalTo(422))
        .body("exception", equalTo("com.natera.test.triangle.exception.LimitExceededException"))
        .body("message", equalTo("Limit exceeded"))
        .body("timestamp", notNullValue())
        .body("path", equalTo("/triangle"));
    }

    private Response createTriangle(String separator, String input) {
        Response triangle = request()
                .body("{\"input\" : \"" + input + "\"}")
                .post("/triangle");
        LOGGER.info("Create Triangle => " + triangle.asString());
        return triangle;
    }

    private Response createTriangle(String input) {
        return createTriangle(":", input);
    }
}
