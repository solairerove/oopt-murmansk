package com.github.solairerove.oopt_murmansk.statistics;

import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsLogger {

    public void logStatisticsByNames(AggregatedVisits aggregatedVisits) {
        for (Map.Entry<String, Map<Integer, Map<YearMonth, Long>>> visitorEntry : aggregatedVisits.visitorsByYearAndMonth().entrySet()) {
            String visitorName = visitorEntry.getKey();
            log.info("ФИО: {}", visitorName);

            for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitorEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                log.info("  Год: {}", year);

                for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                    log.info("    Месяц: {}, Количество посещений: {}", monthEntry.getKey(), monthEntry.getValue());
                }

                long totalDaysForYear = yearEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
                log.info("    Общее количество посещений за год: {}", totalDaysForYear);
            }
        }
    }

    public void logTotalStatisticsByYearsAndMonths(AggregatedVisits aggregatedVisits) {
        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : aggregatedVisits.visitsByYearAndMonth().entrySet()) {
            int year = yearEntry.getKey();
            log.info("Год: {}", year);

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                log.info("  Месяц: {}, Количество посещений: {}", monthEntry.getKey(), monthEntry.getValue());
            }

            log.info("  Общее количество посещений за год: {}", aggregatedVisits.totalVisitsByYear().get(year));
        }
    }
}
