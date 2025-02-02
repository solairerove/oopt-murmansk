package com.github.solairerove.oopt_murmansk.process;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsCalculator;
import com.github.solairerove.oopt_murmansk.excel.OoptExcelFileParser;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsProcessor {

    private final OoptExcelFileParser ooptExcelFileParser;
    private final OoptStatisticsCalculator ooptStatisticsCalculator;

    public void process(String fileName) {
        try {
            Map<String, List<VisitPeriod>> visitsByPerson = this.ooptExcelFileParser.parseExcelFile(fileName);
            this.ooptStatisticsCalculator.calculateAndLogVisitsByYearAndMonth(visitsByPerson);
        } catch (IOException e) {
            log.error("Ошибка при чтении файла", e);
        }
    }
}
