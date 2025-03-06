package com.github.solairerove.oopt_murmansk.excel;

import com.github.solairerove.oopt_murmansk.model.VisitPeriod;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class OoptExcelFileParser {

    public Map<String, List<VisitPeriod>> parseExcelFile(String filePath) throws IOException {
        log.info("Чтение файла: {}", filePath);

        Map<String, List<VisitPeriod>> visitsByPerson = new HashMap<>();

        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            Workbook workbook = new XSSFWorkbook(fileInputStream);
            Sheet sheet = workbook.getSheetAt(0);

            // Проходим по всем строкам, начиная со второй (первая — заголовки)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                if (row.getCell(0) == null) continue;
                if (row.getCell(1) == null) continue;
                if (row.getCell(0).getLocalDateTimeCellValue() == null) continue;
                if (row.getCell(1).getLocalDateTimeCellValue() == null) continue;

//                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
//                LocalDate entryDate = LocalDate. parse(row.getCell(1).getStringCellValue(), formatter); // Дата въезда

                LocalDate entryDate = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
//                LocalDate exitDate = LocalDate.parse(row.getCell(2).getStringCellValue(), formatter); // Дата выезда
                LocalDate exitDate = row.getCell(1).getLocalDateTimeCellValue().toLocalDate();
                String visitorName = row.getCell(4).getStringCellValue().trim().replaceAll("\\s+", " "); // ФИО посетителя (убираем лишние пробелы)
                String blankSent = row.getCell(12).getStringCellValue(); // Бланк отправлен
                String status = row.getCell(13).getStringCellValue(); // Статус

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
}
