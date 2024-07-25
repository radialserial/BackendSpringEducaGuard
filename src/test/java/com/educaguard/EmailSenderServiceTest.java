package com.educaguard;

import com.educaguard.domain.domainException.BusinessRulesException;

import com.educaguard.domain.model.User;
import com.educaguard.domain.repository.UserRepository;
import com.educaguard.domain.service.impl.EmailSenderServiceImpl;
import com.educaguard.utils.Feedback;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailSenderServiceTest {

    @InjectMocks
    private EmailSenderServiceImpl emailSenderService;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private UserRepository repository;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;


    @BeforeEach
    void setUp() {
        mimeMessage = mock(MimeMessage.class);
    }

    @Test
    void testSendEmailSuccess() {
        String to = "test@example.com";
        String token = "testToken";

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(javaMailSender).send(mimeMessage);

        emailSenderService.sendEmail(to, token);

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmailThrowsException() {
        String to = "test@example.com";
        String token = "testToken";

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("Mail sending failed") {}).when(javaMailSender).send(any(MimeMessage.class));

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> emailSenderService.sendEmail(to, token));

        assertTrue(exception.getMessage().contains(Feedback.ERROR_SEND_CONF_EMAIL + to));
    }

    @Test
    void testRecoverAccountSuccess() {
        String to = "test@example.com";
        User user = new User();
        user.setEmail(to);
        user.setToken("oldToken");

        when(repository.findUserByEmail(to)).thenReturn(user);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        emailSenderService.recoverAccount(to);

        verify(repository, times(1)).findUserByEmail(to);
        verify(repository, times(1)).save(any(User.class));
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testRecoverAccountUserNotFound() {
        String to = "test@example.com";

        when(repository.findUserByEmail(to)).thenReturn(null);

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> emailSenderService.recoverAccount(to));

        assertEquals(Feedback.EMPTY_USER, exception.getMessage());
    }

    @Test
    void testRecoverAccountThrowsException() {
        String to = "test@example.com";
        User user = new User();
        user.setEmail(to);
        user.setToken("oldToken");

        when(repository.findUserByEmail(to)).thenReturn(user);
        when(repository.save(user)).thenReturn(user);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailException("Mail sending failed") {}).when(javaMailSender).send(mimeMessage);

        BusinessRulesException exception = assertThrows(BusinessRulesException.class, () -> emailSenderService.recoverAccount(to));

        assertTrue(exception.getMessage().contains(Feedback.ERROR_ACCOUNT_RECOVER + to));
    }
}