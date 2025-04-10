package com.github.solairerove.oopt_murmansk.processor;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsAggregator;
import com.github.solairerove.oopt_murmansk.excel.OoptExcelFileParser;
import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsLogger;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsProcessor {

    private final static String PATH_TO_INPUT = "/Users/solairerove/Downloads/Статистика за февраль, март 2025.xlsx";
    private final static String PATH_TO_OUTPUT = "src/main/resources/%s.txt";

    private final OoptExcelFileParser ooptExcelFileParser;
    private final OoptStatisticsAggregator ooptStatisticsAggregator;
    private final OoptStatisticsLogger ooptStatisticsLogger;
    private final OoptStatisticsWriter ooptStatisticsWriter;

    public void process() {
        try {
            try (FileInputStream fileInputStream = new FileInputStream(PATH_TO_INPUT)) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    Map<String, List<VisitPeriod>> visitsByPerson = this.ooptExcelFileParser.parseExcelFile(sheet);
                    AggregatedVisits aggregatedVisits = this.ooptStatisticsAggregator.aggregateVisits(visitsByPerson);
                    this.ooptStatisticsLogger.logStatisticsByNames(aggregatedVisits);
                    this.ooptStatisticsLogger.logTotalStatisticsByYearsAndMonths(aggregatedVisits);

                    var outputFile = String.format(PATH_TO_OUTPUT, sheetName);
                    this.ooptStatisticsWriter.writeToFile(outputFile, aggregatedVisits);
                }

                workbook.close();
            }

        } catch (IOException e) {
            log.error("Ошибка обработки файла", e);
        }
    }
}
