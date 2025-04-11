package com.github.solairerove.oopt_murmansk.aggregate;

import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class OoptStatisticsAggregator {

    private final OoptStatisticsPeriodMergeService ooptStatisticsPeriodMergeService;

    public OoptStatisticsAggregator(OoptStatisticsPeriodMergeService ooptStatisticsPeriodMergeService) {
        this.ooptStatisticsPeriodMergeService = ooptStatisticsPeriodMergeService;
    }

    // Метод для расчета посещений пребывания по годам и месяцам
    public AggregatedVisits aggregateVisits(Map<String, List<VisitPeriod>> visitsByPerson) {
        // Группировка данных по годам
        Map<Integer, Long> totalVisitsByYear = new TreeMap<>();
        // Группировка данных по годам и месяцам
        Map<Integer, Map<YearMonth, Long>> visitsByYearAndMonth = new TreeMap<>();
        // Группировка данных по ФИО, годам и месяцам
        Map<String, Map<Integer, Map<YearMonth, Long>>> visitorDaysByYearAndMonth = new HashMap<>();

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
                    visitsByYearAndMonth.computeIfAbsent(year, k -> new TreeMap<>());
                    totalVisitsByYear.putIfAbsent(year, 0L);
                    visitorDaysByYearAndMonth.get(visitorName).computeIfAbsent(year, k -> new TreeMap<>());

                    // Увеличиваем счетчик посещений для текущего месяца и года
                    visitsByYearAndMonth.get(year).put(yearMonth, visitsByYearAndMonth.get(year).getOrDefault(yearMonth, 0L) + 1);
                    totalVisitsByYear.put(year, totalVisitsByYear.get(year) + 1);
                    visitorDaysByYearAndMonth.get(visitorName).get(year).put(yearMonth, visitorDaysByYearAndMonth.get(visitorName).get(year).getOrDefault(yearMonth, 0L) + 1);

                    currentDate = currentDate.plusDays(1);
                }
            }
        }

        return new AggregatedVisits(totalVisitsByYear, visitsByYearAndMonth, visitorDaysByYearAndMonth);
    }
}
