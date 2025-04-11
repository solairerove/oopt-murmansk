package com.github.solairerove.oopt_murmansk.statistics;

import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;

import java.time.YearMonth;
import java.util.Map;

public class OoptStatisticsLogger {

    public void logStatisticsByNames(AggregatedVisits aggregatedVisits) {
        for (Map.Entry<String, Map<Integer, Map<YearMonth, Long>>> visitorEntry : aggregatedVisits.visitorsByYearAndMonth().entrySet()) {
            String visitorName = visitorEntry.getKey();
            System.out.printf("ФИО: %s\n", visitorName);

            for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitorEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                System.out.printf("  Год: %s \n", year);

                for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                    System.out.printf("    Месяц: %s, Количество посещений: %s\n", monthEntry.getKey(), monthEntry.getValue());
                }

                long totalDaysForYear = yearEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
                System.out.printf("    Общее количество посещений за год: %s\n", totalDaysForYear);
            }
        }
    }

    public void logTotalStatisticsByYearsAndMonths(AggregatedVisits aggregatedVisits) {
        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : aggregatedVisits.visitsByYearAndMonth().entrySet()) {
            int year = yearEntry.getKey();
            System.out.printf("Год: %s\n", year);

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                System.out.printf("  Месяц: %s, Количество посещений: %s\n", monthEntry.getKey(), monthEntry.getValue());
            }

            System.out.printf("  Общее количество посещений за год: %s\n", aggregatedVisits.totalVisitsByYear().get(year));
        }
    }
}
