package com.natera.quiz;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SimpleTest {

    String token;

    @BeforeClass
    public void configure() {
        RestAssured.baseURI = "https://qa-quiz.natera.com/";
        token = "4a2cb0d2-058e-43aa-a031-0431dce58095";
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
}
