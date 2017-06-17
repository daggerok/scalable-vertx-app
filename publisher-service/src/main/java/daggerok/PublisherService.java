package daggerok;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeEventType;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PublisherService extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    val eb = vertx.eventBus();
    val router = Router.router(vertx);

    router.route("/eventbus/*").handler(eventBusHandler());
    router.route().failureHandler(errorHandler());
    router.route().handler(staticHandler());

    vertx.createHttpServer()
         .requestHandler(router::accept)
         .listen(8080);

    log.info("{} started.", PublisherService.class.getSimpleName());
  }

  private ErrorHandler errorHandler() {
    return ErrorHandler.create(true);
  }

  private StaticHandler staticHandler() {
    return StaticHandler.create().setCachingEnabled(false);
  }

  private SockJSHandler eventBusHandler() {
    BridgeOptions options = new BridgeOptions()
        .addOutboundPermitted(new PermittedOptions().setAddressRegex("auction\\.[0-9]+"));
    return SockJSHandler.create(vertx).bridge(options, event -> {
      if (event.type() == BridgeEventType.SOCKET_CREATED) {
        log.info("A socket was created");
      }
      event.complete(true);
    });
  }

  @Override
  public void stop() throws Exception {
    log.info("{} stopped.", PublisherService.class.getSimpleName());
  }
}
