package daggerok;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class Env {

  @Getter
  private static final Map<String, String> props = new HashMap<>();

  private static final String DB_HOST = "DB_HOST";
  private static final String DB_PORT = "DB_PORT";

  static {
    val envHost = System.getenv(DB_HOST);
    props.put(DB_HOST, isNull(envHost) || envHost.isEmpty() ? "localhost" : envHost);
    val envPort = System.getenv(DB_PORT);
    props.put(DB_PORT, isNull(envPort) || envPort.isEmpty() ? "27017" : envPort);
  }

  @Getter
  private static final String host = props.get(DB_HOST);

  @Getter
  private static final String port = props.get(DB_PORT);
}
