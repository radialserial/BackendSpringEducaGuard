package com.educaguard.api.controller;


import com.educaguard.api.dto.login.LoginOutputDTO;
import com.educaguard.api.dto.others.Message;
import com.educaguard.domain.service.UserService;
import com.educaguard.utils.Feedback;
import io.swagger.oas.annotations.Operation;
import io.swagger.oas.annotations.media.Content;
import io.swagger.oas.annotations.media.Schema;
import io.swagger.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private UserService service;

    @GetMapping("/confirmation/{token}")
    public ResponseEntity<Message> confirmMyCountByReceiveEmail(@PathVariable("token") String token) {
        // Decode the string Base64
        byte[] decodedBytes = Base64.getDecoder().decode(token);
        String tokenDecodedString = new String(decodedBytes);
        service.saveUserAfterConfirmedAccountByEmail(tokenDecodedString);
        // saved
        return new ResponseEntity<Message>(new Message(Feedback.ACCOUNT_CONFIRMED), HttpStatus.OK);
    }


}
