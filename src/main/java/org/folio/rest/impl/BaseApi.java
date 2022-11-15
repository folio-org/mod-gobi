package org.folio.rest.impl;

import static io.vertx.core.Future.succeededFuture;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.HttpHeaders.LOCATION;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.folio.gobi.exceptions.ErrorCodes.GENERIC_ERROR_CODE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.folio.gobi.exceptions.HttpException;
import org.folio.rest.jaxrs.model.Error;
import org.folio.rest.jaxrs.model.Errors;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;

public class BaseApi {
  private final Logger logger = LogManager.getLogger(this.getClass());
  private static final String ERROR_CAUSE = "cause";

  public Response buildOkResponse(Object body) {
    return Response.ok(body, APPLICATION_JSON)
      .build();
  }

  public Response buildNoContentResponse() {
    return Response.noContent()
      .build();
  }

  public Response buildResponseWithLocation(String okapi, String endpoint, Object body) {
    logger.debug("buildResponseWithLocation:: Trying to build response with location endpoint: {}, body: {}, for cache okapi: {}", body, endpoint, okapi);
    try {
      return Response.created(new URI(okapi + endpoint))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .entity(body)
        .build();
    } catch (URISyntaxException e) {
      logger.error("Failed to build response with location", e);
      return Response.created(URI.create(endpoint))
        .header(CONTENT_TYPE, APPLICATION_JSON)
        .header(LOCATION, endpoint)
        .entity(body)
        .build();
    }
  }

  public Void handleErrorResponse(Handler<AsyncResult<Response>> asyncResultHandler, Throwable t) {
    asyncResultHandler.handle(succeededFuture(buildErrorResponse(t)));
    return null;
  }

  public Response buildErrorResponse(Throwable throwable) {
    logger.error("Exception encountered", throwable.getCause());
    final int code = defineErrorCode(throwable);
    final Errors errors = convertToErrors(throwable);
    final Response.ResponseBuilder responseBuilder = createResponseBuilder(code);
    return responseBuilder.header(CONTENT_TYPE, APPLICATION_JSON)
      .entity(errors)
      .build();
  }

  public int defineErrorCode(Throwable throwable) {
    final Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
    if (cause instanceof HttpException) {
      return ((HttpException) cause).getCode();
    }
    return INTERNAL_SERVER_ERROR.getStatusCode();
  }

  private javax.ws.rs.core.Response.ResponseBuilder createResponseBuilder(int code) {
    final javax.ws.rs.core.Response.ResponseBuilder responseBuilder;
    switch (code) {
    case 400:
    case 403:
    case 404:
    case 409:
    case 422:
      responseBuilder = javax.ws.rs.core.Response.status(code);
      break;
    default:
      responseBuilder = javax.ws.rs.core.Response.status(INTERNAL_SERVER_ERROR);
    }
    return responseBuilder;
  }

  public Errors convertToErrors(Throwable throwable) {
    final Throwable cause = throwable.getCause() == null ? throwable : throwable.getCause();
    Errors errors;

    if (cause instanceof HttpException) {
      errors = ((HttpException) cause).getErrors();
      List<Error> errorList = errors.getErrors()
        .stream()
        .map(this::mapToError)
        .collect(toList());

      errors.setErrors(errorList);
      errors.setTotalRecords(errorList.size());
    } else {
      errors = new Errors().withErrors(Collections.singletonList(GENERIC_ERROR_CODE.toError()
        .withAdditionalProperty(ERROR_CAUSE, cause.getMessage())))
        .withTotalRecords(1);
    }
    return errors;
  }

  public boolean isErrorMessageJson(String errorMessage) {
    try {
      new JsonObject(errorMessage);
      return true;
    }
    catch (DecodeException e) {
      return false;
    }
  }

  private Error mapToError(Error error) {
    if (isErrorMessageJson(error.getMessage())) {
      return new JsonObject(error.getMessage()).mapTo(Error.class);
    }
    return error;
  }

}
