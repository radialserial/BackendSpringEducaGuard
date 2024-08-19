package com.educaguard.api.controller;

import com.educaguard.api.dto.others.Message;
import com.educaguard.api.dto.user.UserInputDTO;
import com.educaguard.api.mapper.UserMapper;
import com.educaguard.domain.model.User;
import com.educaguard.domain.service.AwsService;
import com.educaguard.domain.service.EmailSenderService;
import com.educaguard.domain.service.UserService;
import com.educaguard.utils.Feedback;
import com.educaguard.utils.StrongPassword;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/user", produces = {"application/json"})
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private EmailSenderService emailSenderService;

    @Autowired
    private AwsService awsService;

    @PostMapping(value = "/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Message> newUser(@RequestBody @Valid UserInputDTO userInputDto) {
        StrongPassword.isStrong(userInputDto.getPassword());
        User user = service.save(UserMapper.mapperUserInputDTOToUser(userInputDto));
        emailSenderService.sendEmail(user.getEmail(), user.getToken());
        return new ResponseEntity<Message>(new Message(Feedback.SEND_CONF_EMAIL + user.getEmail()), HttpStatus.CREATED);
    }

    @GetMapping("/find/{idUser}")
    public ResponseEntity<?> findUser(@PathVariable Long idUser) {
        return ResponseEntity.ok(UserMapper.mapperUserToUserOutputDTO(service.findUser(idUser)));
    }

    @PostMapping("/{username}/uploadImage")
    public ResponseEntity<String> uploadImage(@PathVariable String username, @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<String>(awsService.uploadFile(file, username), HttpStatus.CREATED);
    }

}
