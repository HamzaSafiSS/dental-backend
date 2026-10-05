package com.dental.dentalbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Binds the "spring.mail.from" property so other beans can inject the sender address.
 *
 * The actual JavaMailSender is auto-configured by Spring Boot's
 * MailSenderAutoConfiguration (spring-boot-starter-mail) — no manual setup needed.
 *
 * Values come from .env / environment variables:
 *   SMTP_HOST — e.g. sandbox.smtp.mailtrap.io
 *   SMTP_PORT — e.g. 2525
 *   SMTP_USER — Mailtrap SMTP username
 *   SMTP_PASS — Mailtrap SMTP password
 *   MAIL_FROM — (optional) sender address, defaults to noreply@dental-clinic.com
 */
@Configuration
public class MailtrapConfig {

    @Value("${spring.mail.from:noreply@dental-clinic.com}")
    private String fromAddress;

    public String getFromAddress() {
        return fromAddress;
    }
}
