package com.educaguard.api.controller;

import com.amazonaws.services.rekognition.model.InvalidS3ObjectException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.educaguard.api.dto.login.LoginInputDTO;
import com.educaguard.api.dto.login.LoginInputGoogleDTO;
import com.educaguard.api.dto.login.LoginOutputDTO;
import com.educaguard.api.dto.others.Message;
import com.educaguard.api.mapper.LoginMapper;
import com.educaguard.domain.model.User;
import com.educaguard.domain.service.AwsService;
import com.educaguard.domain.service.UserService;
import com.educaguard.security.jwt.JwtToken;
import com.educaguard.utils.Feedback;
import com.educaguard.utils.FormatDate;
import com.educaguard.utils.Log;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

@RestController
@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserService service;

    @Autowired
    private AwsService awsService;

    @Autowired
    public AuthenticationManager authenticationManager;
    private final int MAX_ATTEMPTS = 5;

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestBody String jwt, HttpServletRequest request) {
        if (JwtToken.decodeTokenJWT(jwt) != null)
            return new ResponseEntity<>(null, HttpStatus.OK);

        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @PostMapping("/enter")
    public ResponseEntity<?> enter(@RequestBody @Valid LoginInputDTO loginInputDTO, HttpServletRequest request) {
        Message message = new Message();

        // creates a log of the login request
        Log.createSimpleLog(loginInputDTO, request);

        if (service.findUser(loginInputDTO.getUsername())) {
            if (service.attemptsUser(loginInputDTO.getUsername()) < MAX_ATTEMPTS) {
                return processLogin(loginInputDTO);
            } else {
                // At this point, we know that the allowed number of attempts has been exceeded
                // check if there is a waiting date for a new login attempt
                if (service.verifyReleaseDateLogin(loginInputDTO.getUsername())) {

                    // there is a lockout date for login
                    Date releaseDate = service.getDateReleaseLogin(loginInputDTO.getUsername());

                    // check if it is still valid
                    if (releaseDate.after(new Date(System.currentTimeMillis()))) {
                        // if it is not expired yet
                        message.setMessage(Feedback.NEW_ATTEMPT + FormatDate.formatMyDate(releaseDate));
                        return new ResponseEntity<Message>(message, HttpStatus.LOCKED);
                    } else {
                        // time expired
                        service.resetAttemptsAndReleaseLogin(loginInputDTO.getUsername());
                        return processLogin(loginInputDTO);
                    }
                } else {
                    // If it doesn't exist, add the waiting time for a new login attempt for this user
                    Date releaseDate = service.releaseLogin(loginInputDTO.getUsername());
                    message.setMessage(Feedback.EXHAUSTED_ATTEMPTS + FormatDate.formatMyDate(releaseDate));
                    return new ResponseEntity<Message>(message, HttpStatus.LOCKED);
                }
            }
        }
        message.setMessage(Feedback.INVALID_LOGIN);
        return new ResponseEntity<Message>(message, HttpStatus.NOT_ACCEPTABLE);
    }

    @PostMapping("/{username}/recfacial")
    public ResponseEntity<?> enter(@PathVariable String username, @RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Message message = new Message();

        // creates a log of the login request
        Log.createSimpleLogRecFacial(username, request);

        if (service.findUser(username)) {
            if (service.attemptsUser(username) < MAX_ATTEMPTS) {
                return processLoginRecFacial(username, file);
            } else {
                // At this point, we know that the allowed number of attempts has been exceeded
                // check if there is a waiting date for a new login attempt
                if (service.verifyReleaseDateLogin(username)) {

                    // there is a lockout date for login
                    Date releaseDate = service.getDateReleaseLogin(username);

                    // check if it is still valid
                    if (releaseDate.after(new Date(System.currentTimeMillis()))) {
                        // if it is not expired yet
                        message.setMessage(Feedback.NEW_ATTEMPT + FormatDate.formatMyDate(releaseDate));
                        return new ResponseEntity<Message>(message, HttpStatus.LOCKED);
                    } else {
                        // time expired
                        service.resetAttemptsAndReleaseLogin(username);
                        return processLoginRecFacial(username, file);
                    }
                } else {
                    // If it doesn't exist, add the waiting time for a new login attempt for this user
                    Date releaseDate = service.releaseLogin(username);
                    message.setMessage(Feedback.EXHAUSTED_ATTEMPTS + FormatDate.formatMyDate(releaseDate));
                    return new ResponseEntity<Message>(message, HttpStatus.LOCKED);
                }
            }
        }
        message.setMessage(Feedback.INVALID_LOGIN);
        return new ResponseEntity<Message>(message, HttpStatus.NOT_ACCEPTABLE);
    }

    private ResponseEntity<?> processLogin(LoginInputDTO loginInputDTO) {
        try {
            var authenticationToken = new UsernamePasswordAuthenticationToken(loginInputDTO.getUsername(), loginInputDTO.getPassword());
            var auth = authenticationManager.authenticate(authenticationToken);
            // check login
            if (auth.isAuthenticated()) {
                // register login
                User loggedInUser = service.login((User) auth.getPrincipal());
                if (loggedInUser.isStatus()) {
                    // User active
                    return new ResponseEntity<LoginOutputDTO>(LoginMapper.mapperUserToLoginOutputDTO(loggedInUser), HttpStatus.ACCEPTED);
                }
            }
        } catch (AuthenticationException e) {
            // increment attempts
            service.updateAttempts(loginInputDTO.getUsername());
        }

        return new ResponseEntity<Message>(new Message(Feedback.INVALID_LOGIN), HttpStatus.NOT_ACCEPTABLE);
    }

    private ResponseEntity<?> processLoginRecFacial(String username, MultipartFile file) {
        try {
            // check login
            if (awsService.faceMatch(username, file)) {
                // register login
                User loggedInUser = service.loginRecFacial(username);
                if (loggedInUser.isStatus()) {
                    // User active
                    return new ResponseEntity<LoginOutputDTO>(LoginMapper.mapperUserToLoginOutputDTO(loggedInUser), HttpStatus.ACCEPTED);
                }
            }
        } catch (InvalidS3ObjectException e) {
            // increment attempts
            service.updateAttempts(username);
            return new ResponseEntity<Message>(new Message(Feedback.INVALID_LOGIN_NO_IMAGE_FIND), HttpStatus.NOT_ACCEPTABLE);
        }

        return new ResponseEntity<Message>(new Message(Feedback.INVALID_LOGIN), HttpStatus.NOT_ACCEPTABLE);
    }

    @PostMapping("/google")
    public ResponseEntity<LoginOutputDTO> enterWithGoogle(@RequestBody @Valid LoginInputGoogleDTO loginInputGoogleDTO, HttpServletRequest request) {
        // creates a log of the login request
        Log.createGoogleLog(loginInputGoogleDTO, request);
        User user = LoginMapper.mapperLoginInputGoogleDTOToUser(loginInputGoogleDTO);
        User loggedInUser = service.loginWithGoogle(user);
        if (loggedInUser != null && loggedInUser.isStatus()) {
            // Exist email and password // status is true
            LoginOutputDTO loginOutputDTO = new LoginOutputDTO(loggedInUser.getIdUser(), loggedInUser.getToken(), loggedInUser.getRole());
            return new ResponseEntity<LoginOutputDTO>(loginOutputDTO, HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<LoginOutputDTO>((LoginOutputDTO) null, HttpStatus.NO_CONTENT);
    }


}
