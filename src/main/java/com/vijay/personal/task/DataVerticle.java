package com.vijay.personal.task;

import com.vijay.personal.task.objects.Task;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static com.vijay.personal.task.Constants.*;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;

public class DataVerticle extends AbstractVerticle {

  public static final String READ_BUS_ADDRESS = "/internal/file/read";
  public static final String UPSERT_BUS_ADDRESS = "/internal/file/upsert";

  private String dataLocation;
  private List<Task> tasks;

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    dataLocation = config().getString("data.location");
    tasks = new ArrayList<>();

    if (!Files.exists(Paths.get(dataLocation))) {
      System.out.println("data directory doesn't exist, creating..");
      Files.createDirectories(Paths.get(dataLocation));
    } else {
      tasks = Files.list(Paths.get(dataLocation))
        .map(file -> {
          try {
            return new String(Files.readAllBytes(file));
          } catch (IOException e) {
            return null;
          }
        })
        .filter(data -> data != null)
        .map(data -> new JsonObject(data).mapTo(Task.class))
        .sorted(Comparator.comparing(Task::getCreatedOn).reversed())
        .collect(Collectors.toList());
    }
    getVertx().eventBus().consumer(READ_BUS_ADDRESS, this::readFile);
    getVertx().eventBus().consumer(UPSERT_BUS_ADDRESS, this::upsertFile);
    startPromise.complete();
  }

  private void readFile(Message<JsonObject> message) {
    List<JsonObject> returnData = new ArrayList<>();
    if (message.body().containsKey(ACTIVE)) {
      if (message.body().getBoolean(ACTIVE, false)) {
        returnData = tasks.stream()
          .filter(task -> null == task.getCompletedOn())
          .map(JsonObject::mapFrom)
          .collect(Collectors.toList());
      } else {
        returnData = tasks.stream()
          .filter(task -> null != task.getCompletedOn())
          .map(JsonObject::mapFrom)
          .collect(Collectors.toList());
      }
      message.reply(new JsonArray(returnData));
    } else {
      message.fail(BAD_REQUEST.code(), "Unknown read type");
    }
  }

  private void upsertFile(Message<JsonObject> message) {
    try {
      Task upsertData = message.body().mapTo(Task.class);
      if (tasks.contains(upsertData)) {
        Task fromStorage = tasks.get(tasks.indexOf(upsertData));
        fromStorage.setCompletedOn(upsertData.getCompletedOn());
        writeToFileSystem(upsertData.getId(), fromStorage);
      } else {
        tasks.add(upsertData);
        writeToFileSystem(upsertData.getId(), upsertData);
      }
      message.reply(SUCCESS_STATUS);
    } catch (Exception exception) {
      message.fail(INTERNAL_SERVER_ERROR.code(), exception.getMessage());
    }
  }

  private void writeToFileSystem(String fileName, Task content) throws IOException {
    Files.write(
      Paths.get(dataLocation, fileName),
      JsonObject.mapFrom(content).encodePrettily().getBytes(StandardCharsets.UTF_8));
  }

}
