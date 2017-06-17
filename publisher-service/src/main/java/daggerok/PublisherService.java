package daggerok;

import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PublisherService extends AbstractVerticle {

  @Override
  public void start() throws Exception {
    log.info("{} started.", PublisherService.class.getSimpleName());
  }

  @Override
  public void stop() throws Exception {
    log.info("{} stopped.", PublisherService.class.getSimpleName());
  }
}
