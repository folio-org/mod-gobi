package org.folio.rest.utils;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.apache.commons.io.IOUtils;

import io.vertx.junit5.VertxTestContext;

public final class TestUtils {

  private TestUtils() {}

  public static String getMockData(String path) throws IOException {

    try (InputStream resourceAsStream = TestUtils.class.getClassLoader().getResourceAsStream(path)) {
      if (resourceAsStream != null) {
        return IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
      } else {
        StringBuilder sb = new StringBuilder();
        try (Stream<String> lines = Files.lines(Paths.get(path))) {
          lines.forEach(sb::append);
        }
        return sb.toString();
      }
    }
  }

  public static void checkVertxContextCompletion(VertxTestContext context) throws Throwable {
    assertTrue(context.awaitCompletion(30, SECONDS));
    if (context.failed()) {
      throw context.causeOfFailure();
    }
  }

}
