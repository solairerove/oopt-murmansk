package com.github.solairerove.oopt_murmansk.statistics;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsAggregator;
import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsService {

    private final OoptStatisticsAggregator ooptStatisticsAggregator;

    public void aggregateAndLogVisitsByYearAndMonth(Map<String, List<VisitPeriod>> visitsByPerson) {
        AggregatedVisits aggregatedVisits = this.ooptStatisticsAggregator.aggregateVisits(visitsByPerson);
        var totalVisitsByYear = aggregatedVisits.totalVisitsByYear();
        var visitsByYearAndMonth = aggregatedVisits.visitsByYearAndMonth();
        var visitorDaysByYearAndMonth = aggregatedVisits.visitorDaysByYearAndMonth();

        // Строка для записи в файл
        StringBuilder output = new StringBuilder();
        // TODO: move to logger service
        // Логируем результат по годам и месяцам для каждого человека
        for (Map.Entry<String, Map<Integer, Map<YearMonth, Long>>> visitorEntry : visitorDaysByYearAndMonth.entrySet()) {
            String visitorName = visitorEntry.getKey();
            log.info("ФИО: {}", visitorName);
            output.append("ФИО: ").append(visitorName).append("\n");

            for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitorEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                log.info("  Год: {}", year);
                output.append("  Год: ").append(year).append("\n");

                for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                    log.info("    Месяц: {}, Количество посещений: {}", monthEntry.getKey(), monthEntry.getValue());
                    output.append("    Месяц: ").append(monthEntry.getKey()).append(", Количество посещений: ").append(monthEntry.getValue()).append("\n");
                }

                // Логируем общее количество посещений за год для текущего человека
                long totalDaysForYear = yearEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
                log.info("    Общее количество посещений за год: {}", totalDaysForYear);
                output.append("    Общее количество посещений за год: ").append(totalDaysForYear).append("\n");
            }
        }

        // Логируем общий результат по годам и месяцам
        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitsByYearAndMonth.entrySet()) {
            int year = yearEntry.getKey();
            log.info("Год: {}", year);
            output.append("Год: ").append(year).append("\n");

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                log.info("  Месяц: {}, Количество посещений: {}", monthEntry.getKey(), monthEntry.getValue());
                output.append("  Месяц: ").append(monthEntry.getKey()).append(", Количество посещений: ").append(monthEntry.getValue()).append("\n");
            }

            // Логируем общее количество посещений за год
            log.info("  Общее количество посещений за год: {}", totalVisitsByYear.get(year));
            output.append("  Общее количество посещений за год: ").append(totalVisitsByYear.get(year)).append("\n");
        }

        // TODO: move to writer service
        // Записываем результат в файл
        try {
            Files.write(Paths.get("src/main/resources/output.txt"), output.toString().getBytes());
            log.info("Результат записан в файл output.txt");
        } catch (IOException e) {
            log.error("Ошибка при записи в файл", e);
        }
    }
}
