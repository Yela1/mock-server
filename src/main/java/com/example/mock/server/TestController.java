package com.example.mock.server;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;

public class TestController {

    public void viewController(){

        get("http://127.0.0.1:8081/view/controller?someId=test123");
    }

    public void get(String uri) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response=null;
        HttpGet get = new HttpGet(uri);
        try {
            response=client.execute(get);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
