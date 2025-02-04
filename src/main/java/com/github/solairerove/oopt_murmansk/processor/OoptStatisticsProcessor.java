package com.github.solairerove.oopt_murmansk.processor;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsAggregator;
import com.github.solairerove.oopt_murmansk.excel.OoptExcelFileParser;
import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsLogger;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsWriter;
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

    private final static String pathToInput = "permissions-090125155637.xlsx";
    private final static String pathToOutput = "src/main/resources/output.txt";

    private final OoptExcelFileParser ooptExcelFileParser;
    private final OoptStatisticsAggregator ooptStatisticsAggregator;
    private final OoptStatisticsLogger ooptStatisticsLogger;
    private final OoptStatisticsWriter ooptStatisticsWriter;

    public void process() {
        try {
            Map<String, List<VisitPeriod>> visitsByPerson = this.ooptExcelFileParser.parseExcelFile(pathToInput);
            AggregatedVisits aggregatedVisits = this.ooptStatisticsAggregator.aggregateVisits(visitsByPerson);
            this.ooptStatisticsLogger.logStatisticsByNames(aggregatedVisits);
            this.ooptStatisticsLogger.logTotalStatisticsByYearsAndMonths(aggregatedVisits);
            this.ooptStatisticsWriter.writeToFile(pathToOutput, aggregatedVisits);
        } catch (IOException e) {
            log.error("Ошибка обработки файла", e);
        }
    }
}
