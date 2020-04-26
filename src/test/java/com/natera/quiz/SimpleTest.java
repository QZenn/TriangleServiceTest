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
    public void testTest() {
        RequestSpecification request = quizRequest();

        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");
        request.body(requestParams.toJSONString());

        Response response = request.post("/triangle");

        System.out.println("Response Body is =>  " + response.asString());
    }

    @Test
    public void testGetAllTriangles() {
        RequestSpecification request = quizRequest();
        Response response = request.get("/triangle/" + "all");

        Assert.assertEquals(response.statusCode(), 200, "Status code is invalid");

        System.out.println("Response Body is =>  " + response.asString());
    }

    @Test
    public void testDeleteTriangle() {
        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");

        Response postTriangle = quizRequest()
                .body(requestParams.toJSONString())
                .post("/triangle");

        Assert.assertEquals(postTriangle.getStatusCode(), 200);

        String triangleId = postTriangle.jsonPath().get("id");

        Response triangleResponse = quizRequest().get("/triangle/" + triangleId);
        Assert.assertEquals(triangleResponse.getStatusCode(), 200);
        System.out.println("Response Body is =>  " + triangleResponse.asString());

        Response deleteResponse = quizRequest().delete("/triangle/" + triangleId);
        Assert.assertEquals(deleteResponse.statusCode(), 200);
        System.out.println("Response Body is =>  " + deleteResponse.asString());

        Response check = quizRequest().get("/triangle/" + triangleId);
        Assert.assertEquals(check.statusCode(), 404);
        System.out.println("Response Body is =>  " + check.asString());
    }
}
