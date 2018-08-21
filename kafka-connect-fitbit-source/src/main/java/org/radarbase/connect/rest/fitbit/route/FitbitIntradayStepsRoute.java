package org.radarbase.connect.rest.fitbit.route;

import io.confluent.connect.avro.AvroData;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import org.radarbase.connect.rest.fitbit.converter.FitbitIntradayStepsAvroConverter;
import org.radarbase.connect.rest.fitbit.request.FitbitRequestGenerator;
import org.radarbase.connect.rest.fitbit.request.FitbitRestRequest;
import org.radarbase.connect.rest.fitbit.user.FitbitUser;
import org.radarbase.connect.rest.fitbit.user.FitbitUserRepository;

public class FitbitIntradayStepsRoute extends FitbitPollingRoute {
  private final FitbitIntradayStepsAvroConverter converter;

  public FitbitIntradayStepsRoute(FitbitRequestGenerator generator,
      FitbitUserRepository userRepository, AvroData avroData) {
    super(generator, userRepository, "intraday_steps");
    this.converter = new FitbitIntradayStepsAvroConverter(avroData);
  }

  @Override
  protected String getUrlFormat(String baseUrl) {
    return baseUrl + "/1/user/%s/activities/steps/date/%s/1d/1min/time/%s/%s.json?timezone=UTC";
  }

  protected FitbitRestRequest makeRequest(FitbitUser user) {
    ZonedDateTime startDate = this.getOffset(user)
        .atZone(ZoneOffset.UTC)
        .plus(Duration.ofMinutes(1))
        .truncatedTo(ChronoUnit.MINUTES);

    ZonedDateTime endDate = startDate.withHour(23).withMinute(59);

    Instant startInstant = startDate.toInstant();

    return newRequest(user, startInstant, startInstant.plus(ONE_DAY),
        user.getFitbitUserId(), DATE_FORMAT.format(startDate),
        TIME_FORMAT.format(startDate), TIME_FORMAT.format(endDate));
  }

  @Override
  public FitbitIntradayStepsAvroConverter converter() {
    return converter;
  }
}
