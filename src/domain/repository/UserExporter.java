package domain.repository;

import domain.model.User;

import java.io.File;
import java.util.List;

public interface UserExporter {
    void exportUsers(List<User> users, File targetFile);
}
