package com.github.solairerove.oopt_murmansk.processor;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsAggregator;
import com.github.solairerove.oopt_murmansk.excel.OoptExcelFileParser;
import com.github.solairerove.oopt_murmansk.model.AggregatedVisits;
import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsLogger;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsWriter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class OoptStatisticsProcessor {

    private final static String INPUT_FILENAME = "input.xlsx";
    private final static String OUTPUT_FILENAME_TEMPLATE = "%s.txt";

    private final OoptExcelFileParser ooptExcelFileParser;
    private final OoptStatisticsAggregator ooptStatisticsAggregator;
    private final OoptStatisticsLogger ooptStatisticsLogger;
    private final OoptStatisticsWriter ooptStatisticsWriter;

    public OoptStatisticsProcessor(OoptExcelFileParser ooptExcelFileParser, OoptStatisticsAggregator ooptStatisticsAggregator, OoptStatisticsLogger ooptStatisticsLogger, OoptStatisticsWriter ooptStatisticsWriter) {
        this.ooptExcelFileParser = ooptExcelFileParser;
        this.ooptStatisticsAggregator = ooptStatisticsAggregator;
        this.ooptStatisticsLogger = ooptStatisticsLogger;
        this.ooptStatisticsWriter = ooptStatisticsWriter;
    }

    public void process() {
        try {

            Path workingDir = Paths.get(System.getProperty("user.dir"));
            Path inputPath = workingDir.resolve(INPUT_FILENAME);
            System.out.printf("Рабочая директория: %s \n", workingDir);

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
            System.out.printf("%s, %s%n", "Ошибка обработки файла \n", e);
        }
    }
}
