package com.vijay.personal.task;

import com.vijay.personal.task.objects.Task;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.time.Instant;
import java.util.UUID;

import static com.vijay.personal.task.Constants.ACTIVE;
import static com.vijay.personal.task.Constants.SUCCESS_RESPONSE_DELIVERY_OPTIONS;
import static com.vijay.personal.task.DataVerticle.READ_BUS_ADDRESS;
import static com.vijay.personal.task.DataVerticle.UPSERT_BUS_ADDRESS;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class TasksVerticle extends AbstractVerticle {

  private static final String READ_ACTIVE_BUS_ADDRESS = "/api/tasks/read/active";
  private static final String READ_COMPLETED_BUS_ADDRESS = "/api/tasks/read/completed";
  private static final String TASK_ADD_BUS_ADDRESS = "/api/tasks/add";
  private static final String TASK_COMPLETED_BUS_ADDRESS = "/api/tasks/complete";

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    vertx.eventBus().consumer(READ_ACTIVE_BUS_ADDRESS, this::readActive);
    vertx.eventBus().consumer(READ_COMPLETED_BUS_ADDRESS, this::readCompleted);
    vertx.eventBus().consumer(TASK_ADD_BUS_ADDRESS, this::addTask);
    vertx.eventBus().consumer(TASK_COMPLETED_BUS_ADDRESS, this::markCompleted);
    startPromise.complete();
  }

  private void readActive(Message<Void> message) {
    vertx.eventBus().request(
      READ_BUS_ADDRESS,
      new JsonObject().put(ACTIVE, true),
      result -> {
      if (result.succeeded()) {
        message.reply(result.result().body(), SUCCESS_RESPONSE_DELIVERY_OPTIONS);
      } else {
        message.fail(INTERNAL_SERVER_ERROR.code(), "Unable to fetch active tasks");
      }
    });
  }

  private void readCompleted(Message<Void> message) {
    vertx.eventBus().request(
      READ_BUS_ADDRESS,
      new JsonObject().put(ACTIVE, false),
      result -> {
        if (result.succeeded()) {
          message.reply(result.result().body(), SUCCESS_RESPONSE_DELIVERY_OPTIONS);
        } else {
          message.fail(INTERNAL_SERVER_ERROR.code(), "Unable to fetch active tasks");
        }
      });
  }

  private void addTask(Message<JsonObject> message) {
    Task task = message.body().mapTo(Task.class);
    task.setCreatedOn(Instant.now()).setId(UUID.randomUUID().toString());
    if (null == task.getDueBy()) {
      task.setDueBy(Instant.now());
    }
    vertx.eventBus().request(UPSERT_BUS_ADDRESS, JsonObject.mapFrom(task), result -> {
      if (result.succeeded()) {
        message.reply(result.result().body(), SUCCESS_RESPONSE_DELIVERY_OPTIONS);
      } else {
        message.fail(INTERNAL_SERVER_ERROR.code(), "Unable to add task");
      }
    });
  }

  private void markCompleted(Message<JsonObject> message) {
    Task task = message.body().mapTo(Task.class);
    task.setCompletedOn(Instant.now());
    vertx.eventBus().request(UPSERT_BUS_ADDRESS, JsonObject.mapFrom(task), result -> {
      if (result.succeeded()) {
        message.reply(result.result().body(), SUCCESS_RESPONSE_DELIVERY_OPTIONS);
      } else {
        message.fail(INTERNAL_SERVER_ERROR.code(), "Unable to add task");
      }
    });
  }

}
