package data.local.exporter;

import domain.model.User;
import domain.repository.UserExporter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PoiUserExcelExporter implements UserExporter {
    @Override
    public void exportUsers(List<User> users, File targetFile) {
        // Создаем родительские папки, если пользователь указал путь в подкаталог
        if (targetFile.getParentFile() != null) {
            targetFile.getParentFile().mkdirs();
        }

        // Создаем Excel-книгу (.xlsx) в памяти
        try (Workbook workbook = new XSSFWorkbook()){
            Sheet sheet = workbook.createSheet("Users");

            // Стиль для шапки (жирный шрифт)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Создаем строку заголовков
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Username", "Email", "Role"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Заполняем Excel таблицу
            int rowNum = 1;
            for (User user : users) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(user.getId());
                row.createCell(1).setCellValue(user.getUsername() != null ? user.getUsername() : "");
                row.createCell(2).setCellValue(user.getEmail() != null ? user.getEmail() : "");
                row.createCell(3).setCellValue(user.getRole() != null ? user.getRole().name() : "");
            }

            // Автоподбор ширины колонок
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Запись в файл
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to export users to Excel", e);
        }
    }
}
