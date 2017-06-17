package daggerok;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import static daggerok.Env.*;
import static java.lang.String.format;

@Slf4j
public class DataService extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    val cs = format("mongodb://%s:%s", getHost(), getPort());
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

    log.info("app started: {}", Env.getProps());
  }

  @Override
  public void stop() throws Exception {
    log.info("app stopped.");
  }
}
