package com.github.solairerove.oopt_murmansk_parser;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@SpringBootApplication
public class OoptMurmanskParserApplication implements CommandLineRunner {

    private static Logger LOG = LoggerFactory.getLogger(OoptMurmanskParserApplication.class);

    public static void main(String[] args) {
        LOG.info("STARTING THE APPLICATION");
        SpringApplication.run(OoptMurmanskParserApplication.class, args);
        LOG.info("APPLICATION FINISHED");
    }

    @Override
    public void run(String... args) {
        LOG.info("EXECUTING : command line runner");

        try {
            Map<String, List<VisitPeriod>> visitsByPerson = readExcelFile("permissions-090125155637.xlsx"); // Укажите имя вашего файла
            calculateAndLogVisitsByYearAndMonth(visitsByPerson);
        } catch (IOException e) {
            LOG.error("Ошибка при чтении файла", e);
        }
    }

    // Метод для чтения Excel-файла и фильтрации данных
    private Map<String, List<VisitPeriod>> readExcelFile(String fileName) throws IOException {
        LOG.info("Чтение файла: {}", fileName);

        Map<String, List<VisitPeriod>> visitsByPerson = new HashMap<>();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Файл не найден: " + fileName);
            }

            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Проходим по всем строкам, начиная со второй (первая — заголовки)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
                // Чтение данных из колонок
                LocalDate entryDate = LocalDate.parse(row.getCell(2).getStringCellValue(), formatter); // Дата въезда
                LocalDate exitDate = LocalDate.parse(row.getCell(3).getStringCellValue(), formatter); // Дата выезда
                String visitorName = row.getCell(4).getStringCellValue().trim().replaceAll("\\s+", " "); // ФИО посетителя (убираем лишние пробелы)
                String blankSent = row.getCell(18).getStringCellValue(); // Бланк отправлен
                String status = row.getCell(19).getStringCellValue(); // Статус

                // Фильтрация данных
                if ("Да".equalsIgnoreCase(blankSent) && "Выдано".equalsIgnoreCase(status)) {
                    VisitPeriod period = new VisitPeriod(entryDate, exitDate);

                    // Добавляем период в список для текущего ФИО
                    visitsByPerson.computeIfAbsent(visitorName, k -> new ArrayList<>()).add(period);
                }
            }

            workbook.close();
        }

        return visitsByPerson;
    }

    // Метод для расчета дней пребывания по годам и месяцам
    private void calculateAndLogVisitsByYearAndMonth(Map<String, List<VisitPeriod>> visitsByPerson) {
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
            List<VisitPeriod> mergedPeriods = mergePeriods(periods);

            // Инициализация структур данных для текущего ФИО
            visitorDaysByYearAndMonth.putIfAbsent(visitorName, new HashMap<>());

            // Считаем дни по годам и месяцам
            for (VisitPeriod period : mergedPeriods) {
                LocalDate currentDate = period.getEntryDate();
                LocalDate endDate = period.getExitDate();

                while (!currentDate.isAfter(endDate)) {
                    YearMonth yearMonth = YearMonth.from(currentDate);
                    int year = yearMonth.getYear();

                    // Инициализация структур данных для года, если они еще не созданы
                    daysByYearAndMonth.computeIfAbsent(year, k -> new TreeMap<>());
                    totalDaysByYear.putIfAbsent(year, 0L);
                    visitorDaysByYearAndMonth.get(visitorName).computeIfAbsent(year, k -> new TreeMap<>());

                    // Увеличиваем счетчик дней для текущего месяца и года
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
            LOG.info("ФИО: {}", visitorName);
            output.append("ФИО: ").append(visitorName).append("\n");

            for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : visitorEntry.getValue().entrySet()) {
                int year = yearEntry.getKey();
                LOG.info("  Год: {}", year);
                output.append("  Год: ").append(year).append("\n");

                for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                    LOG.info("    Месяц: {}, Количество дней: {}", monthEntry.getKey(), monthEntry.getValue());
                    output.append("    Месяц: ").append(monthEntry.getKey()).append(", Количество дней: ").append(monthEntry.getValue()).append("\n");
                }

                // Логируем общее количество дней за год для текущего человека
                long totalDaysForYear = yearEntry.getValue().values().stream().mapToLong(Long::longValue).sum();
                LOG.info("    Общее количество дней за год: {}", totalDaysForYear);
                output.append("    Общее количество дней за год: ").append(totalDaysForYear).append("\n");
            }
        }

        // Логируем общий результат по годам и месяцам
        for (Map.Entry<Integer, Map<YearMonth, Long>> yearEntry : daysByYearAndMonth.entrySet()) {
            int year = yearEntry.getKey();
            LOG.info("Год: {}", year);
            output.append("Год: ").append(year).append("\n");

            for (Map.Entry<YearMonth, Long> monthEntry : yearEntry.getValue().entrySet()) {
                LOG.info("  Месяц: {}, Количество дней: {}", monthEntry.getKey(), monthEntry.getValue());
                output.append("  Месяц: ").append(monthEntry.getKey()).append(", Количество дней: ").append(monthEntry.getValue()).append("\n");
            }

            // Логируем общее количество дней за год
            LOG.info("  Общее количество дней за год: {}", totalDaysByYear.get(year));
            output.append("  Общее количество дней за год: ").append(totalDaysByYear.get(year)).append("\n");
        }

        // Записываем результат в файл
        try {
            Files.write(Paths.get("src/main/resources/output.txt"), output.toString().getBytes());
            LOG.info("Результат записан в файл output.txt");
        } catch (IOException e) {
            LOG.error("Ошибка при записи в файл", e);
        }
    }

    // Метод для объединения пересекающихся периодов
    private List<VisitPeriod> mergePeriods(List<VisitPeriod> periods) {
        if (periods.isEmpty()) {
            return periods;
        }

        // Сортируем периоды по дате въезда
        periods.sort(Comparator.comparing(VisitPeriod::getEntryDate));

        List<VisitPeriod> mergedPeriods = new ArrayList<>();
        VisitPeriod current = periods.get(0);

        for (int i = 1; i < periods.size(); i++) {
            VisitPeriod next = periods.get(i);

            // Если периоды пересекаются или соприкасаются, объединяем их
            if (current.getExitDate().isAfter(next.getEntryDate()) || current.getExitDate().equals(next.getEntryDate())) {
                current = new VisitPeriod(
                        current.getEntryDate().isBefore(next.getEntryDate()) ? current.getEntryDate() : next.getEntryDate(),
                        current.getExitDate().isAfter(next.getExitDate()) ? current.getExitDate() : next.getExitDate()
                );
            } else {
                mergedPeriods.add(current);
                current = next;
            }
        }

        mergedPeriods.add(current);
        return mergedPeriods;
    }

    // Внутренний класс для хранения периода посещения
    private static class VisitPeriod {
        private final LocalDate entryDate;
        private final LocalDate exitDate;

        public VisitPeriod(LocalDate entryDate, LocalDate exitDate) {
            this.entryDate = entryDate;
            this.exitDate = exitDate;
        }

        public LocalDate getEntryDate() {
            return entryDate;
        }

        public LocalDate getExitDate() {
            return exitDate;
        }

        @Override
        public String toString() {
            return entryDate + " - " + exitDate;
        }
    }
}