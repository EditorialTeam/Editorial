package domain.service;

import domain.model.User;
import domain.repository.UserRepository;
import domain.validator.IdValidator;
import domain.validator.UserValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, new UserValidator(), new IdValidator());
    }

    @Test
    void addPersistsValidUser() {
        User user = user();

        userService.add(user);

        verify(userRepository).addUser(user);
    }

    @Test
    void addDoesNotPersistInvalidUser() {
        User invalid = new User(0, "ab", "bad", "", null);

        assertThrows(IllegalArgumentException.class, () -> userService.add(invalid));
        verify(userRepository, never()).addUser(any());
    }

    @Test
    void editUpdatesFieldsAndSaves() {
        User existing = user();
        when(userRepository.getUserById(2)).thenReturn(existing);

        userService.edit(2, "bob", "bob@example.com", "new-hash", User.Role.EDITOR);

        assertEquals("bob", existing.getUsername());
        assertEquals("bob@example.com", existing.getEmail());
        assertEquals("new-hash", existing.getPasswordHash());
        assertEquals(User.Role.EDITOR, existing.getRole());
        verify(userRepository).editUser(existing);
    }

    @Test
    void editRejectsInvalidId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userService.edit(0, "bob", "bob@example.com", "hash", User.Role.AUTHOR)
        );
        verify(userRepository, never()).getUserById(0);
    }

    @Test
    void editDoesNotSaveWhenValidationFails() {
        when(userRepository.getUserById(2)).thenReturn(user());

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.edit(2, "bob", "not-an-email", "hash", User.Role.AUTHOR)
        );
        verify(userRepository, never()).editUser(any());
    }

    @Test
    void getByIdReturnsRepositoryResult() {
        User existing = user();
        when(userRepository.getUserById(2)).thenReturn(existing);

        assertSame(existing, userService.getById(2));
    }

    @Test
    void getAllReturnsRepositoryResult() {
        List<User> users = List.of(user());
        when(userRepository.getUsers()).thenReturn(users);

        assertSame(users, userService.getAll());
    }

    @Test
    void deleteDeletesAfterIdCheck() {
        userService.delete(2);

        verify(userRepository).deleteUser(2);
    }

    @Test
    void getAndDeleteRejectInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> userService.getById(0));
        assertThrows(IllegalArgumentException.class, () -> userService.delete(-5));
        verify(userRepository, never()).getUserById(0);
        verify(userRepository, never()).deleteUser(-5);
    }

    private static User user() {
        return new User(2, "alice", "alice@example.com", "hash", User.Role.AUTHOR);
    }
}
