package daggerok;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.isNull;

@Slf4j
public class DataService extends AbstractVerticle {

  static final String DB_HOST = "DB_HOST";
  static final String DB_PORT = "DB_PORT";
  static final Map<String, String> props = new HashMap<>();

  static {
    val envHost = System.getenv(DB_HOST);
    props.put(DB_HOST, isNull(envHost) || envHost.isEmpty() ? "localhost" : envHost);
    val envPort = System.getenv(DB_PORT);
    props.put(DB_PORT, isNull(envPort) || envPort.isEmpty() ? "27017" : envPort);
  }

  static String host() {
    return props.get(DB_HOST);
  }

  static String port() {
    return props.get(DB_PORT);
  }

  @Override
  public void start() throws Exception {

    val cs = format("mongodb://%s:%s", host(), port());
    val eb = vertx.eventBus();
    val mc = MongoClient.createShared(
        vertx,
        new JsonObject()
            .put("connection_string", cs)
            .put("db_name", "db"));

    eb.consumer("data.save", message -> {
      mc.insert("utx", (JsonObject) message.body(), insert -> {
        if (insert.succeeded()) {
          log.info("message saved: {}", message);
          eb.publish("data.update", message.body());
        } else log.error("cannot save message: {}", message);
      });
    });

    eb.consumer("data.find", message -> {
      val findOptions = new FindOptions((JsonObject) message.body());
      mc.findWithOptions("utx", new JsonObject(), findOptions, find -> {
        if (find.failed()) {
          log.error("cannot find opts: {}", find.cause());
          message.fail(400, find.cause().getLocalizedMessage());
        } else {
          log.info("found by opts: {}", findOptions);
          message.reply(new JsonArray(find.result()));
        }
      });
    });

    log.info("app started: {}", props);
  }

  @Override
  public void stop() throws Exception {
    log.info("app stopped.");
  }
}
