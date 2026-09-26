package data.local.exporter;

import domain.model.Article;
import domain.repository.ArticleExporter;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PoiArticleExcelExporter implements ArticleExporter {

    @Override
    public void exportArticles(List<Article> articles, File targetFile) {
        // Создаем Excel-книгу (.xlsx) в памяти
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Articles");

            // Стиль для шапки (жирный шрифт)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Создаем строку заголовков
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Author ID", "Title", "Content", "Status", "Published At"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Заполняем строки данными статей
            int rowNum = 1;
            for (Article article : articles) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(article.getId());
                row.createCell(1).setCellValue(article.getAuthorId());
                row.createCell(2).setCellValue(article.getTitle() != null ? article.getTitle() : "");
                row.createCell(3).setCellValue(article.getContent() != null ? article.getContent() : "");
                row.createCell(4).setCellValue(article.getStatus() != null ? article.getStatus().name() : "");
                row.createCell(5).setCellValue(article.getPublishedAt() != null ? article.getPublishedAt() : "");
            }

            // Автоматически подгоняем ширину колонок под текст
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Записываем сформированную книгу в файл на диск
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                workbook.write(fos);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Failed to export articles to Excel", e);
        }
    }
}