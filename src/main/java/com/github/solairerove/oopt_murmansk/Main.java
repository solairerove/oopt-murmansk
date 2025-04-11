package com.github.solairerove.oopt_murmansk;

import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsAggregator;
import com.github.solairerove.oopt_murmansk.aggregate.OoptStatisticsPeriodMergeService;
import com.github.solairerove.oopt_murmansk.excel.OoptExcelFileParser;
import com.github.solairerove.oopt_murmansk.processor.OoptStatisticsProcessor;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsLogger;
import com.github.solairerove.oopt_murmansk.statistics.OoptStatisticsWriter;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== OOPT Murmansk Parser ===");

        OoptExcelFileParser ooptExcelFileParser = new OoptExcelFileParser();
        OoptStatisticsPeriodMergeService ooptStatisticsPeriodMergeService = new OoptStatisticsPeriodMergeService();
        OoptStatisticsAggregator ooptStatisticsAggregator = new OoptStatisticsAggregator(ooptStatisticsPeriodMergeService);
        OoptStatisticsLogger ooptStatisticsLogger = new OoptStatisticsLogger();
        OoptStatisticsWriter ooptStatisticsWriter = new OoptStatisticsWriter();
        OoptStatisticsProcessor ooptStatisticsProcessor = new OoptStatisticsProcessor(
                ooptExcelFileParser,
                ooptStatisticsAggregator,
                ooptStatisticsLogger,
                ooptStatisticsWriter
        );
        ooptStatisticsProcessor.process();

        System.out.println("=== Done ===");
    }
}