
package com.example.DATN.service.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@localhost}")
    private String from;

    @Override
    public void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Send mail failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void sendHtml(List<String> toList, String subject, String html) {
        for (String to : toList) {
            if (to == null || to.isBlank()) continue;
            sendHtml(to.trim(), subject, html);
        }
    }

    // tiện dùng ở chỗ khác
    public static List<String> parseEmails(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }
}
