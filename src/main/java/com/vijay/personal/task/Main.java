package com.vijay.personal.task;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        JsonObject config = new JsonObject(new String(Files.readAllBytes(Paths.get("server-config.json"))));
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainVerticle(), new DeploymentOptions().setConfig(config));
    }

}
