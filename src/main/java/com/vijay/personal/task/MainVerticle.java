package com.vijay.personal.task;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;

public class MainVerticle extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startFuture) {
        vertx.deployVerticle(
                "com.vijay.personal.task.Server",
                new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(
          "com.vijay.personal.task.TasksVerticle",
          new DeploymentOptions().setConfig(config()));
        vertx.deployVerticle(
          "com.vijay.personal.task.DataVerticle",
          new DeploymentOptions().setConfig(config()));
        startFuture.complete();
    }
}