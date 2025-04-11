package com.github.solairerove.oopt_murmansk.statistics;

import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.Map;

public class OoptStatisticsWriter {

    public void writeToFile(Path outputPath, AggregatedVisits aggregatedVisits) {
        var totalVisitsByYear = aggregatedVisits.totalVisitsByYear();
        var visitsByYearAndMonth = aggregatedVisits.visitsByYearAndMonth();
        var visitorDaysByYearAndMonth = aggregatedVisits.visitorsByYearAndMonth();

        StringBuilder output = new StringBuilder();
        for (Map.Entry<String, Map<Integer, Map<YearMonth, Long>>> visitorEntry : visitorDaysByYearAndMonth.entrySet()) {
            String visitorName = visitorEntry.getKey();
            output.append("ФИО: ").append(visitorName).append("\n");

            for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitorEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                output.append("  Год: ").append(year).append("\n");

                for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                    output.append("    Месяц: ").append(monthEntry.getKey()).append(", Количество посещений: ").append(monthEntry.getValue()).append("\n");
                }

                long totalDaysForYear = yearEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
                output.append("    Общее количество посещений за год: ").append(totalDaysForYear).append("\n");
            }
        }

        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitsByYearAndMonth.entrySet()) {
            int year = yearEntry.getKey();
            output.append("Год: ").append(year).append("\n");

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                output.append("  Месяц: ").append(monthEntry.getKey()).append(", Количество посещений: ").append(monthEntry.getValue()).append("\n");
            }

            output.append("  Общее количество посещений за год: ").append(totalVisitsByYear.get(year)).append("\n");
        }

        try {
            Files.write(outputPath, output.toString().getBytes());
            System.out.println("Результат записан в файл output.txt");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл");
        }
    }
}
