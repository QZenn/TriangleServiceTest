package com.natera.quiz;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTest {

    String token;

    private static Logger LOGGER = null;

    @BeforeClass
    public void configure() {
        RestAssured.baseURI = "https://qa-quiz.natera.com/";
        token = "4a2cb0d2-058e-43aa-a031-0431dce58095";

        System.setProperty("log4j.configurationFile","log4j2.xml");
        LOGGER = LogManager.getLogger(this.getClass().getName());
        LOGGER.info("Configuration completed. Start Test Run...");
    }

    private RequestSpecification quizRequest() {
        RequestSpecification request = RestAssured.given()
                .header("Content-Type", "application/json")
                .header("X-User", token);

        return request;
    }

    @Test
    public void createTriangle() {
        RequestSpecification request = quizRequest();

        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");
        request.body(requestParams.toJSONString());

        Response response = request.post("/triangle");
        LOGGER.info(response.asString());
        Assert.assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void getAllTriangles() {
        RequestSpecification request = quizRequest();
        Response response = request.get("/triangle/" + "all");
        LOGGER.info("All Triangles => " + response.asString());
        Assert.assertEquals(response.statusCode(), 200);
    }

    @Test
    public void deleteTriangle() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");

        Response createTriangle = quizRequest()
                .body(requestParams.toJSONString())
                .post("/triangle");
        LOGGER.info("Create Triangle => " + createTriangle.asString());
        Assert.assertEquals(createTriangle.getStatusCode(), 200);

        String triangleId = createTriangle.jsonPath().get("id");

        Response getTriangle = quizRequest().get("/triangle/" + triangleId);
        LOGGER.info("Get Triangle => " + getTriangle.asString());
        Assert.assertEquals(getTriangle.getStatusCode(), 200);

        Response deleteTriangle = quizRequest().delete("/triangle/" + triangleId);
        LOGGER.info("Delete Triangle = > " + deleteTriangle.asString());
        Assert.assertEquals(deleteTriangle.statusCode(), 200);

        Response checkDeletion = quizRequest().get("/triangle/" + triangleId);
        LOGGER.info("Check Deletion => " + checkDeletion.asString());
        Assert.assertEquals(checkDeletion.statusCode(), 404);
    }
}
