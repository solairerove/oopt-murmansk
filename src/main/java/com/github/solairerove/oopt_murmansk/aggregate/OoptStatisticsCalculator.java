package com.github.solairerove.oopt_murmansk.aggregate;

import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsCalculator {

    private final OoptStatisticsPeriodMergeService ooptStatisticsPeriodMergeService;

    // Метод для расчета посещений пребывания по годам и месяцам
    public void calculateAndLogVisitsByYearAndMonth(Map<String, List<VisitPeriod>> visitsByPerson) {
        // Группировка данных по годам и месяцам
        Map<Integer, Map<YearMonth, Long>> daysByYearAndMonth = new TreeMap<>();
        Map<Integer, Long> totalDaysByYear = new TreeMap<>();

        // Группировка данных по ФИО, годам и месяцам
        Map<String, Map<Integer, Map<YearMonth, Long>>> visitorDaysByYearAndMonth = new HashMap<>();

        // Строка для записи в файл
        StringBuilder output = new StringBuilder();

        for (Map.Entry<String, List<VisitPeriod>> entry : visitsByPerson.entrySet()) {
            String visitorName = entry.getKey();
            List<VisitPeriod> periods = entry.getValue();

            // Объединяем пересекающиеся периоды
            List<VisitPeriod> mergedPeriods = this.ooptStatisticsPeriodMergeService.mergePeriods(periods);

            // Инициализация структур данных для текущего ФИО
            visitorDaysByYearAndMonth.putIfAbsent(visitorName, new HashMap<>());

            // Считаем дни по годам и месяцам
            for (VisitPeriod period : mergedPeriods) {
                LocalDate currentDate = period.entryDate();
                LocalDate endDate = period.exitDate();

                while (!currentDate.isAfter(endDate)) {
                    YearMonth yearMonth = YearMonth.from(currentDate);
                    int year = yearMonth.getYear();

                    // Инициализация структур данных для года, если они еще не созданы
                    daysByYearAndMonth.computeIfAbsent(year, k -> new TreeMap<>());
                    totalDaysByYear.putIfAbsent(year, 0L);
                    visitorDaysByYearAndMonth.get(visitorName).computeIfAbsent(year, k -> new TreeMap<>());

                    // Увеличиваем счетчик посещений для текущего месяца и года
                    daysByYearAndMonth.get(year).put(yearMonth, daysByYearAndMonth.get(year).getOrDefault(yearMonth, 0L) + 1);
                    totalDaysByYear.put(year, totalDaysByYear.get(year) + 1);
                    visitorDaysByYearAndMonth.get(visitorName).get(year).put(yearMonth, visitorDaysByYearAndMonth.get(visitorName).get(year).getOrDefault(yearMonth, 0L) + 1);

                    currentDate = currentDate.plusDays(1);
                }
            }
        }

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
        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : daysByYearAndMonth.entrySet()) {
            int year = yearEntry.getKey();
            log.info("Год: {}", year);
            output.append("Год: ").append(year).append("\n");

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                log.info("  Месяц: {}, Количество посещений: {}", monthEntry.getKey(), monthEntry.getValue());
                output.append("  Месяц: ").append(monthEntry.getKey()).append(", Количество посещений: ").append(monthEntry.getValue()).append("\n");
            }

            // Логируем общее количество посещений за год
            log.info("  Общее количество посещений за год: {}", totalDaysByYear.get(year));
            output.append("  Общее количество посещений за год: ").append(totalDaysByYear.get(year)).append("\n");
        }

        // Записываем результат в файл
        try {
            Files.write(Paths.get("src/main/resources/output.txt"), output.toString().getBytes());
            log.info("Результат записан в файл output.txt");
        } catch (IOException e) {
            log.error("Ошибка при записи в файл", e);
        }
    }
}
