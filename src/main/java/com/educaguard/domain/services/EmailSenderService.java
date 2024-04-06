package com.educaguard.domain.services;

public interface EmailSenderService {
    public void sendEmail(String to, String token);
    public void recoverAccount(String to);

}
