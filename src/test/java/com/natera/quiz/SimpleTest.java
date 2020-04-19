package com.natera.quiz;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

public class SimpleTest {

    @Test
    public void testTest() {
        RestAssured.baseURI = "https://qa-quiz.natera.com/";

        RequestSpecification request = RestAssured.given();

        JSONObject requestParams = new JSONObject();
        requestParams.put("input", "3;4;5");

        request.header("Content-Type", "application/json");
        request.header("X-User", "4a2cb0d2-058e-43aa-a031-0431dce58095");

        request.body(requestParams.toJSONString());

        Response response = request.post("/triangle");

        System.out.println("Response Body is =>  " + response.asString());
    }
}
