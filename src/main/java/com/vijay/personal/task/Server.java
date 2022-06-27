package com.vijay.personal.task;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.MultiMap;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.ReplyException;
import io.vertx.core.eventbus.ReplyFailure;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.commons.lang3.StringUtils;

import static com.vijay.personal.task.Constants.HTTP_STATUS_CODE;

public class Server extends AbstractVerticle {
    private static final String ARRAY_START = "[";
    private static final String OBJECT_START = "{";
    private static final String BASE_URI = "/api/*";
    private static final String PAGES_URI = "/*";
    private static final String HEALTH = "/health";

    @Override
    public void start(Promise<Void> fut) {
        try {
            Router router = Router.router(vertx);
            router.route(HEALTH).handler(this::routeHealthRequest);
            router.route(BASE_URI).handler(BodyHandler.create());
            router.route(BASE_URI).handler(this::routeRequest);
            router.route(PAGES_URI).handler(StaticHandler.create().setIndexPage("index.html")
                    .setCachingEnabled(false)
                    .setFilesReadOnly(false));

            vertx.createHttpServer(new HttpServerOptions()
                            .setPort(config().getInteger("server.http.port"))
                            .setHost("0.0.0.0"))
                    .requestHandler(router)
                    .listen(result -> {
                        if (result.succeeded()) {
                            fut.complete();
                        } else {
                            fut.fail(result.cause());
                        }
                    });
        } catch (Exception exp) {
            fut.fail(exp);
        }
    }

    private void routeRequest(RoutingContext routingContext) {
        String body = routingContext.getBodyAsString();
        String requestURI = routingContext.normalizedPath();

        DeliveryOptions options = new DeliveryOptions().setHeaders(routingContext.request().headers());
        getVertx().eventBus().request(requestURI, transformBody(body), options, response -> {
            HttpServerResponse serverResponse = routingContext.response();
            if (response.succeeded()) {
                MultiMap headers = response.result().headers();
                int statusCode = getStatusCode(headers);
                serverResponse.setStatusCode(statusCode);
                serverResponse.headers().addAll(response.result().headers());
                if (response.result().body() == null) {
                    serverResponse.end();
                } else {
                    serverResponse.end(response.result().body().toString());
                }
            } else {
                ReplyException e = (ReplyException) response.cause();
                setErrorCode(serverResponse, e).end(response.cause().toString());
            }
        });
    }

    private Object transformBody(String body) {
        return StringUtils.isNotEmpty(body)
                ? StringUtils.startsWith(body, ARRAY_START) ? new JsonArray(body)
                : (StringUtils.startsWith(body, OBJECT_START) ? new JsonObject(body) : body) : null;
    }

    private int getStatusCode(MultiMap headers) {
        if (headers.contains(HTTP_STATUS_CODE)) {
            int status = Integer.parseInt(headers.get(HTTP_STATUS_CODE));
            return validateStatusCode(status);
        }
        return HttpResponseStatus.OK.code();
    }

    private int validateStatusCode(int status) {
        if (status >= 100 && status <= 511) {
            return status;
        }
        return HttpResponseStatus.INTERNAL_SERVER_ERROR.code();
    }

    private HttpServerResponse setErrorCode(HttpServerResponse response, ReplyException exception) {
        if (exception.failureCode() == ReplyFailure.TIMEOUT.toInt()) {
            return response.setStatusCode(HttpResponseStatus.GATEWAY_TIMEOUT.code());
        } else if (exception.failureCode() == ReplyFailure.NO_HANDLERS.toInt()) {
            return response.setStatusCode(HttpResponseStatus.NOT_IMPLEMENTED.code());
        } else {
            return response.setStatusCode(validateStatusCode(exception.failureCode()));
        }
    }

    private void routeHealthRequest(RoutingContext routingContext) {
        HttpServerResponse serverResponse = routingContext.response();
        serverResponse.setStatusCode(200);
        serverResponse.end("Health Check successful");
    }

}
