package domain.usecase;

import domain.model.Article;
import domain.repository.ArticleExporter;
import domain.repository.ArticleRepository;

import java.io.File;
import java.util.List;

public class ExportArticlesToExcelUseCase {
    private final ArticleRepository articleRepository;
    private final ArticleExporter articleExporter;

    public ExportArticlesToExcelUseCase(ArticleRepository articleRepository, ArticleExporter articleExporter) {
        this.articleRepository = articleRepository;
        this.articleExporter = articleExporter;
    }

    public void execute(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be empty"); // Если нет пути к файлу возвращаем ошибку
        }

        // Автоматически дописываем расширение .xlsx, если пользователь забыл
        if (!filePath.endsWith(".xlsx")) {
            filePath += ".xlsx";
        }

        List<Article> articles = articleRepository.getArticles();
        if (articles.isEmpty()) {
            throw new IllegalStateException("No articles to export"); // При отсутствие статей выбрасываем ошибку
        }

        File file = new File(filePath);
        articleExporter.exportArticles(articles, file); // Экспортируем статьи в выбранный файл
    }
}