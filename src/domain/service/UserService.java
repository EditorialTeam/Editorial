package domain.service;

import domain.model.User;
import domain.repository.UserRepository;
import domain.validator.IdValidator;
import domain.validator.UserValidator;

import java.util.List;

public class UserService {
    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final IdValidator idValidator;

    public UserService(
            UserRepository userRepository,
            UserValidator userValidator,
            IdValidator idValidator
    ) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.idValidator = idValidator;
    }

    public List<User> getAll() {
        return userRepository.getUsers();
    }

    public User getById(int userId) {
        idValidator.validate(userId, "User ID");
        return userRepository.getUserById(userId);
    }

    public void add(User user) {
        userValidator.validate(user);
        userRepository.addUser(user);
    }

    public void edit(
            int userId,
            String username,
            String email,
            String passwordHash,
            User.Role role
    ) {
        idValidator.validate(userId, "User ID");

        User user = userRepository.getUserById(userId);
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordHash);
        user.setRole(role);

        userValidator.validate(user);
        userRepository.editUser(user);
    }

    public void delete(int userId) {
        idValidator.validate(userId, "User ID");
        userRepository.deleteUser(userId);
    }
}
