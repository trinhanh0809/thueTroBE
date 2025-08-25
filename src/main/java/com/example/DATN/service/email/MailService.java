package com.example.DATN.service.email;

import java.util.List;

public interface MailService {
    void sendHtml(String to, String subject, String html);
    void sendHtml(List<String> toList, String subject, String html);
}
