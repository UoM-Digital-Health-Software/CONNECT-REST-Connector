package org.radarbase.connect.rest.fitbit.converter;

import static org.radarbase.connect.rest.fitbit.request.FitbitRequestGenerator.JSON_READER;

import com.fasterxml.jackson.databind.JsonNode;
import io.confluent.connect.avro.AvroData;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.avro.generic.IndexedRecord;
import org.apache.kafka.connect.data.SchemaAndValue;
import org.apache.kafka.connect.source.SourceRecord;
import org.radarbase.connect.rest.converter.PayloadToSourceRecordConverter;
import org.radarbase.connect.rest.fitbit.request.FitbitRestRequest;
import org.radarbase.connect.rest.fitbit.user.FitbitUser;
import org.radarbase.connect.rest.request.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FitbitAvroConverter implements PayloadToSourceRecordConverter {
  private static final Logger logger = LoggerFactory.getLogger(FitbitAvroConverter.class);
  private static final Map<String, TimeUnit> TIME_UNIT_MAP = new HashMap<>();

  static {
    TIME_UNIT_MAP.put("minute", TimeUnit.MINUTES);
    TIME_UNIT_MAP.put("second", TimeUnit.SECONDS);
    TIME_UNIT_MAP.put("hour", TimeUnit.HOURS);
    TIME_UNIT_MAP.put("day", TimeUnit.DAYS);
    TIME_UNIT_MAP.put("millisecond", TimeUnit.MILLISECONDS);
    TIME_UNIT_MAP.put("nanosecond", TimeUnit.NANOSECONDS);
    TIME_UNIT_MAP.put("microsecond", TimeUnit.MICROSECONDS);
  }

  private final AvroData avroData;

  public FitbitAvroConverter(AvroData avroData) {
    this.avroData = avroData;
  }

  @Override
  public Collection<SourceRecord> convert(RestRequest restRequest, Response response)
      throws IOException {
    ResponseBody body = response.body();
    if (body == null) {
      throw new IOException("Failed to read body");
    }
    JsonNode activities = JSON_READER.readTree(body.charStream());

    FitbitUser user = ((FitbitRestRequest) restRequest).getUser();
    final SchemaAndValue key = user.getObservationKey(avroData);
    double timeReceived = System.currentTimeMillis() / 1000d;

    return processRecords((FitbitRestRequest)restRequest, activities,timeReceived)
        .filter(Objects::nonNull)
        .map(t -> {
          SchemaAndValue avro = avroData.toConnectData(t.value.getSchema(), t.value);
          Map<String, ?> offset = Collections.singletonMap(
              TIMESTAMP_OFFSET_KEY, t.sourceOffset.toEpochMilli());

          return new SourceRecord(restRequest.getPartition(), offset, t.topic,
              key.schema(), key.value(), avro.schema(), avro.value());
        })
        .collect(Collectors.toList());
  }

  protected abstract Stream<TopicData> processRecords(FitbitRestRequest request, JsonNode root,
      double timeReceived);

  protected static int getRecordInterval(JsonNode root, int defaultValue) {
    JsonNode type = root.get("datasetType");
    JsonNode interval = root.get("datasetInterval");
    if (type == null || interval == null) {
      logger.warn("Failed to get data interval; using {} instead", defaultValue);
      return defaultValue;
    }
    return (int)TIME_UNIT_MAP
        .getOrDefault(type.asText(), TimeUnit.SECONDS)
        .toSeconds(interval.asLong());
  }

  protected static <T> Stream<T> iterableToStream(Iterable<T> iter) {
    return StreamSupport.stream(iter.spliterator(), false);
  }

  protected static class TopicData {
    Instant sourceOffset;
    final String topic;
    final IndexedRecord value;

    public TopicData(Instant sourceOffset, String topic, IndexedRecord value) {
      this.sourceOffset = sourceOffset;
      this.topic = topic;
      this.value = value;
    }
  }
}
