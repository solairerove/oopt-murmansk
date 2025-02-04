package com.github.solairerove.oopt_murmansk.model;

import java.time.YearMonth;
import java.util.Map;

public record AggregatedVisits(
        // Группировка данных по годам
        Map<Integer, Long> totalVisitsByYear,
        // Группировка данных по годам и месяцам
        Map<Integer, Map<YearMonth, Long>> visitsByYearAndMonth,
        // Группировка данных по ФИО, годам и месяцам
        Map<String, Map<Integer, Map<YearMonth, Long>>> visitorsByYearAndMonth) {
}
