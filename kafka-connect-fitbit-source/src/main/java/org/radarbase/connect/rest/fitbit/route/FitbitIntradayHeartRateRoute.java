package org.radarbase.connect.rest.fitbit.route;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

import io.confluent.connect.avro.AvroData;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.radarbase.connect.rest.fitbit.converter.FitbitIntradayHeartRateAvroConverter;
import org.radarbase.connect.rest.fitbit.request.FitbitRequestGenerator;
import org.radarbase.connect.rest.fitbit.request.FitbitRestRequest;
import org.radarbase.connect.rest.fitbit.user.FitbitUser;
import org.radarbase.connect.rest.fitbit.user.FitbitUserRepository;

public class FitbitIntradayHeartRateRoute extends FitbitPollingRoute {
  private static final String ROUTE_NAME = "heart_rate";
  private final FitbitIntradayHeartRateAvroConverter converter;

  public FitbitIntradayHeartRateRoute(FitbitRequestGenerator generator,
      FitbitUserRepository userRepository, AvroData avroData) {
    super(generator, userRepository, ROUTE_NAME);
    this.converter = new FitbitIntradayHeartRateAvroConverter(avroData);
  }

  @Override
  protected String getUrlFormat(String baseUrl) {
    return baseUrl + "/1/user/%s/activities/heart/date/%s/1d/1sec/time/%s/%s.json?timezone=UTC";
  }

  protected FitbitRestRequest makeRequest(FitbitUser user) {
    ZonedDateTime startDate = this.getOffset(user)
        .atZone(ZoneOffset.UTC)
        .truncatedTo(ChronoUnit.MINUTES);

    ZonedDateTime endDate = startDate.withHour(23).withMinute(59).withSecond(59);

    Instant startInstant = startDate.toInstant();

    return newRequest(user, startInstant, startInstant.plus(ONE_DAY),
        user.getFitbitUserId(), DATE_FORMAT.format(startDate),
        ISO_LOCAL_TIME.format(startDate), ISO_LOCAL_TIME.format(endDate));
  }

  @Override
  public FitbitIntradayHeartRateAvroConverter converter() {
    return converter;
  }
}
