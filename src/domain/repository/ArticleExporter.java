package domain.repository;

import domain.model.Article;
import java.io.File;
import java.util.List;

public interface ArticleExporter {
    // Метод экспорта списка статей в целевой файл
    void exportArticles(List<Article> articles, File targetFile);
}
