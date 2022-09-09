package eu.coatrack.admin.service.user;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class MailConfig {
    @Value("${ygg.mail.verify-new-users-via-mail}")
    private boolean verifyNewUsersViaEmail;

    @Value("${ygg.mail.sender.user}")
    private String mail_sender_user;

    @Value("${ygg.mail.sender.password}")
    private String mail_sender_password;

    @Value("${ygg.mail.server.url}")
    private String mail_server_url;

    @Value("${ygg.mail.server.port}")
    private int mail_server_port;

    @Value("${ygg.mail.verification.server.url}")
    private String mail_verification_server_url;

    @Value("${ygg.mail.sender.from}")
    private String mail_sender_from;
}
