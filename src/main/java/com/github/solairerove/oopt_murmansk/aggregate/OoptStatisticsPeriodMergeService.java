package com.github.solairerove.oopt_murmansk.aggregate;

import com.github.solairerove.oopt_murmansk.model.VisitPeriod;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class OoptStatisticsPeriodMergeService {

    // Метод для объединения пересекающихся периодов
    public List<VisitPeriod> mergePeriods(List<VisitPeriod> periods) {
        if (periods.isEmpty()) {
            return periods;
        }

        // Сортируем периоды по дате въезда
        periods.sort(Comparator.comparing(VisitPeriod::entryDate));

        List<VisitPeriod> mergedPeriods = new ArrayList<>();
        VisitPeriod current = periods.get(0);

        for (int i = 1; i < periods.size(); i++) {
            VisitPeriod next = periods.get(i);

            // Если периоды пересекаются или соприкасаются, объединяем их
            if (current.exitDate().isAfter(next.entryDate()) || current.exitDate().equals(next.entryDate())) {
                current = new VisitPeriod(
                        current.entryDate().isBefore(next.entryDate()) ? current.entryDate() : next.entryDate(),
                        current.exitDate().isAfter(next.exitDate()) ? current.exitDate() : next.exitDate()
                );
            } else {
                mergedPeriods.add(current);
                current = next;
            }
        }

        mergedPeriods.add(current);
        return mergedPeriods;
    }
}
