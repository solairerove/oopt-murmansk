package com.github.solairerove.oopt_murmansk.excel;

import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OoptExcelFileParser {

    private final static String DATE_PATTERN = "dd.MM.yyyy";
    private final static String BLANK_SET_FILTER = "Да";
    private final static String STATUS_FILTER = "Выдано";

    public Map<String, List<VisitPeriod>> parseExcelFile(Sheet sheet) throws IOException {
        Map<String, List<VisitPeriod>> visitsByPerson = new HashMap<>();
        // Проходим по всем строкам, начиная со второй (первая — заголовки)
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            if (row.getCell(2) == null) continue;
            if (row.getCell(3) == null) continue;
            if (row.getCell(2).getStringCellValue() == null) continue;
            if (row.getCell(3).getStringCellValue() == null) continue;

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            LocalDate entryDate = LocalDate.parse(row.getCell(2).getStringCellValue(), formatter); // Дата въезда
            LocalDate exitDate = LocalDate.parse(row.getCell(3).getStringCellValue(), formatter); // Дата выезда
            String visitorName = row.getCell(4).getStringCellValue().trim().replaceAll("\\s+", " "); // ФИО посетителя (убираем лишние пробелы)
            String blankSent = row.getCell(18).getStringCellValue(); // Бланк отправлен
            String status = row.getCell(19).getStringCellValue(); // Статус

            // Фильтрация данных
            if (BLANK_SET_FILTER.equalsIgnoreCase(blankSent) && STATUS_FILTER.equalsIgnoreCase(status)) {
                VisitPeriod period = new VisitPeriod(entryDate, exitDate);

                // Добавляем период в список для текущего ФИО
                visitsByPerson.computeIfAbsent(visitorName, k -> new ArrayList<>()).add(period);
            }
        }

        return visitsByPerson;
    }
}
