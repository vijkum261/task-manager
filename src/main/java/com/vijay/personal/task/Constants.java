package com.vijay.personal.task;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;

public class Constants {
    public static final String HTTP_STATUS_CODE = "httpStatusCode";

    public static final DeliveryOptions SUCCESS_RESPONSE_DELIVERY_OPTIONS = new DeliveryOptions()
            .addHeader(HTTP_STATUS_CODE, String.valueOf(HttpResponseStatus.OK.code()))
            .addHeader("Content-Type", "application/json");
    public static final JsonObject SUCCESS_STATUS = new JsonObject().put("status", true);
    public static final JsonObject FAILED_STATUS = new JsonObject().put("status", false);
    public static final String ID = "id";
    public static final String ACTIVE = "active";
}