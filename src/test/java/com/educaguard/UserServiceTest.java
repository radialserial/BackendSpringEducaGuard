package com.educaguard;

import com.educaguard.api.dto.password.NewPasswordInputDTO;
import com.educaguard.domain.domainException.BusinessRulesException;
import com.educaguard.domain.enums.Roles;
<<<<<<< HEAD
import com.educaguard.domain.model.User;
import com.educaguard.domain.repository.UserRepository;
import com.educaguard.domain.service.impl.UserServiceImpl;
=======
import com.educaguard.domain.models.User;
import com.educaguard.domain.repository.UserRepository;
import com.educaguard.domain.services.impl.UserServiceImpl;
>>>>>>> 53cf8df674394f66d517d96bdd6fd12695fa8e63
import com.educaguard.utils.Feedback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository repository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(repository);
    }

    @Test
    void testSaveSuccess() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password");

        when(repository.findUserByEmail(user.getEmail())).thenReturn(null);
        when(repository.findUserByUsername(user.getUsername())).thenReturn(null);
        when(repository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setIdUser(1L);
            return savedUser;
        });

        User savedUser = userService.save(user);

        assertNotNull(savedUser);
        assertNotNull(savedUser.getToken());
        assertFalse(savedUser.isStatus());
        assertEquals(Roles.ROLE_USER, savedUser.getRole());
        verify(repository, times(1)).save(user);
    }

    @Test
    void testSaveUserAlreadyExists() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");

        when(repository.findUserByEmail(user.getEmail())).thenReturn(user);

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> userService.save(user));

        assertEquals(Feedback.USER_EXIST, exception.getMessage());
    }

    @Test
    void testSaveUserAfterConfirmedAccountByEmailSuccess() {
        String token = "validToken";
        User user = new User();
        user.setToken(token);

        when(repository.findUserByToken(token)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);

        User updatedUser = userService.saveUserAfterConfirmedAccountByEmail(token);

        assertTrue(updatedUser.isStatus());
        verify(repository, times(1)).findUserByToken(token);
        verify(repository, times(1)).save(user);
    }

    @Test
    void testSaveUserAfterConfirmedAccountByEmailTokenNotFound() {
        String token = "invalidToken";

        when(repository.findUserByToken(token)).thenReturn(Optional.empty());

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> userService.saveUserAfterConfirmedAccountByEmail(token));

        assertEquals(Feedback.ERROR_CONFIRMATION_ACCOUNT, exception.getMessage());
    }

    @Test
    void testLoginSuccess() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");

        when(repository.save(user)).thenReturn(user);

        User loggedInUser = userService.login(user);

        assertNotNull(loggedInUser.getToken());
        assertEquals(0, loggedInUser.getAttempts());
        assertNull(loggedInUser.getReleaseLogin());
        verify(repository, times(1)).save(user);
    }

    @Test
    void testFindUserByIdSuccess() {
        Long userId = 1L;
        User user = new User();
        user.setIdUser(userId);

        when(repository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.findUser(userId);

        assertEquals(userId, foundUser.getIdUser());
        verify(repository, times(1)).findById(userId);
    }

    @Test
    void testFindUserByIdNotFound() {
        Long userId = 1L;

        when(repository.findById(userId)).thenReturn(Optional.empty());

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> userService.findUser(userId));

        assertEquals(Feedback.NOT_EXIST_USER_ID + userId, exception.getMessage());
    }

    @Test
    void testFindUserByUsernameExists() {
        String username = "testuser";

        when(repository.findUserByUsername(username)).thenReturn(new User());

        assertTrue(userService.findUser(username));
        verify(repository, times(1)).findUserByUsername(username);
    }

    @Test
    void testFindUserByUsernameNotExists() {
        String username = "testuser";

        when(repository.findUserByUsername(username)).thenReturn(null);

        assertFalse(userService.findUser(username));
        verify(repository, times(1)).findUserByUsername(username);
    }

    @Test
    void testAttemptsUser() {
        String username = "testuser";

        when(repository.attemptsUser(username)).thenReturn(1);

        int attempts = userService.attemptsUser(username);

        assertEquals(1, attempts);
        verify(repository, times(1)).attemptsUser(username);
    }

    @Test
    void testGetDateReleaseLogin() {
        String username = "testuser";
        Date date = new Date();

        when(repository.getDateReleaseLogin(username)).thenReturn(date);

        Date resultDate = userService.getDateReleaseLogin(username);

        assertEquals(date, resultDate);
        verify(repository, times(1)).getDateReleaseLogin(username);
    }

    @Test
    void testVerifyReleaseDateLogin() {
        String username = "testuser";

        when(repository.getDateReleaseLogin(username)).thenReturn(new Date());

        assertTrue(userService.verifyReleaseDateLogin(username));
        verify(repository, times(1)).getDateReleaseLogin(username);
    }

    @Test
    void testResetAttemptsAndReleaseLogin() {
        String username = "testuser";

        doNothing().when(repository).resetAttemptsAndReleaseLogin(username);

        userService.resetAttemptsAndReleaseLogin(username);

        verify(repository, times(1)).resetAttemptsAndReleaseLogin(username);
    }

    @Test
    void testUpdatePasswordSuccess() {
        NewPasswordInputDTO newPasswordInputDTO = new NewPasswordInputDTO();
        newPasswordInputDTO.setToken("validToken");
        newPasswordInputDTO.setNewpassword("newPassword");
        User user = new User();
        user.setToken("validToken");

        when(repository.findUserByToken(newPasswordInputDTO.getToken())).thenReturn(Optional.of(user));
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updatedUser = userService.updatePassword(newPasswordInputDTO);

        assertNotNull(updatedUser);
        assertTrue(passwordEncoder.matches("newPassword", updatedUser.getPassword()));
        verify(repository, times(1)).findUserByToken(newPasswordInputDTO.getToken());
        verify(repository, times(1)).save(user);
    }

    @Test
    void testUpdatePasswordTokenNotFound() {
        NewPasswordInputDTO newPasswordInputDTO = new NewPasswordInputDTO();
        newPasswordInputDTO.setToken("invalidToken");
        newPasswordInputDTO.setNewpassword("newPassword");

        when(repository.findUserByToken(newPasswordInputDTO.getToken())).thenReturn(Optional.empty());

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> userService.updatePassword(newPasswordInputDTO));

        assertEquals(Feedback.ERROR_PASSWORD_CHANGE, exception.getMessage());
    }

    @Test
    void testFindUserByUsername() {
        String username = "testuser";
        User user = new User();
        user.setUsername(username);

        when(repository.findUserByUsername(username)).thenReturn(user);

        User foundUser = userService.findUserByUsername(username);

        assertEquals(username, foundUser.getUsername());
        verify(repository, times(1)).findUserByUsername(username);
    }
}
