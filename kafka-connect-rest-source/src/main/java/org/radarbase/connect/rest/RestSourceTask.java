package org.radarbase.connect.rest;

import org.apache.kafka.connect.errors.ConnectException;
import org.apache.kafka.connect.source.SourceRecord;
import org.apache.kafka.connect.source.SourceTask;
import org.radarbase.connect.rest.request.RequestGenerator;
import org.radarbase.connect.rest.request.RestRequest;
import org.radarbase.connect.rest.util.VersionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

import static org.radarbase.connect.rest.util.ThrowingFunction.tryOrNull;

public class RestSourceTask extends SourceTask {
  private static Logger logger = LoggerFactory.getLogger(RestSourceTask.class);

  private RequestGenerator requestGenerator;

  @Override
  public void start(Map<String, String> map) {
    RestSourceConnectorConfig connectorConfig;
    try {
      Class<?> connector = Class.forName(map.get("connector.class"));
      connectorConfig = ((AbstractRestSourceConnector)connector.newInstance()).getConfig(map);
    } catch (ClassNotFoundException e) {
      throw new ConnectException("Connector " + map.get("connector.class") + " not found", e);
    } catch (IllegalAccessException | InstantiationException e) {
      throw new ConnectException("Connector " + map.get("connector.class")
          + " could not be instantiated", e);
    }
    requestGenerator = connectorConfig.getRequestGenerator();
    requestGenerator.setOffsetStorageReader(context.offsetStorageReader());
  }

  @Override
  public List<SourceRecord> poll() throws InterruptedException {
    long timeout = requestGenerator.getTimeOfNextRequest() - System.currentTimeMillis();
    if (timeout > 0) {
      logger.info("Waiting {} milliseconds for next available request", timeout);
      Thread.sleep(timeout);
    }

    LongAdder requestsGenerated = new LongAdder();

    List<SourceRecord> requests = requestGenerator.requests()
        .peek(r -> {
          logger.info("Requesting {}", r.getRequest().url());
          requestsGenerated.increment();
        })
        .flatMap(tryOrNull(RestRequest::handleRequest,
            (r, ex) -> logger.warn("Failed to make request: {}", ex.toString())))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    logger.info("Processed {} records from {} URLs", requests.size(), requestsGenerated.sum());

    return requests;
  }

  @Override
  public void stop() {
    logger.debug("Stopping source task");
  }

  @Override
  public String version() {
    return VersionUtil.getVersion();
  }
}
