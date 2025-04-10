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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OoptStatisticsProcessor {

    private final static String INPUT_FILENAME = "input.xlsx";
    private final static String OUTPUT_FILENAME_TEMPLATE = "%s.txt";

    private final OoptExcelFileParser ooptExcelFileParser;
    private final OoptStatisticsAggregator ooptStatisticsAggregator;
    private final OoptStatisticsLogger ooptStatisticsLogger;
    private final OoptStatisticsWriter ooptStatisticsWriter;

    public void process() {
        try {

            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path inputPath = workingDir.resolve(INPUT_FILENAME);
            log.info("Рабочая директория: {}", workingDir);

            try (FileInputStream fileInputStream = new FileInputStream(inputPath.toFile())) {
                Workbook workbook = new XSSFWorkbook(fileInputStream);
                int numberOfSheets = workbook.getNumberOfSheets();
                for (int i = 0; i < numberOfSheets; i++) {
                    Sheet sheet = workbook.getSheetAt(i);
                    String sheetName = sheet.getSheetName();

                    Map<String, List<VisitPeriod>> visitsByPerson = this.ooptExcelFileParser.parseExcelFile(sheet);
                    AggregatedVisits aggregatedVisits = this.ooptStatisticsAggregator.aggregateVisits(visitsByPerson);
                    this.ooptStatisticsLogger.logStatisticsByNames(aggregatedVisits);
                    this.ooptStatisticsLogger.logTotalStatisticsByYearsAndMonths(aggregatedVisits);

                    String outputName = String.format(OUTPUT_FILENAME_TEMPLATE, sheetName);
                    Path outputPath = workingDir.resolve(outputName);
                    this.ooptStatisticsWriter.writeToFile(outputPath, aggregatedVisits);
                }

                workbook.close();
            }

        } catch (IOException e) {
            log.error("Ошибка обработки файла", e);
        }
    }
}
