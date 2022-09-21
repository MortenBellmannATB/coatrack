package eu.coatrack.admin.service.user;

import eu.coatrack.admin.model.repository.UserRepository;
import eu.coatrack.admin.validator.UserValidator;
import eu.coatrack.api.CreditAccount;
import eu.coatrack.api.User;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

@AllArgsConstructor
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private MailConfig mailConfig;

    @Autowired
    private UserValidator userValidator;

    public User getAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(auth.getName());
        return user;
    }

    public void registerUser(User user, BindingResult bindingResult) {
        userValidator.validate(user, bindingResult);

        user.setInitialized(Boolean.FALSE);

        CreditAccount creditAccount = new CreditAccount();
        user.setAccount(creditAccount);
        creditAccount.setUser(user);

        userRepository.save(user);

        if (mailConfig.isVerifyNewUsersViaEmail())
            sendVerificationEmail(user);
    }


    public User save(User user) {
        return userRepository.save(user);
    }

    public User findById(long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    public void verifyUserViaVerificationCode(long userId, String emailVerificationCode) {
        User user = findById(userId);

        if (user != null && user.getEmailVerifiedUrl().equals(emailVerificationCode)) {
            user.setEmailVerified(Boolean.TRUE);
            userRepository.save(user);
        }
    }

    private void sendVerificationEmail(User user) {
        JavaMailSenderImpl mailSender = getMailSenderFromConfig();
        prepareMailProperties(mailSender.getJavaMailProperties());
        try {
            MimeMessage message = prepareVerificationMailForUser(mailSender.createMimeMessage(), user);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareMailProperties(Properties props) {
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");
        props.put("mail.smtp.ssl.enable", "true");
    }

    private JavaMailSenderImpl getMailSenderFromConfig() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailConfig.getMail_server_url());
        mailSender.setPort(mailConfig.getMail_server_port());
        mailSender.setProtocol("smtp");

        mailSender.setUsername(mailConfig.getMail_sender_user());
        mailSender.setPassword(mailConfig.getMail_sender_password());
        return mailSender;
    }

    private MimeMessage prepareVerificationMailForUser(MimeMessage message, User user) throws MessagingException {
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(user.getEmail());
        helper.setFrom(mailConfig.getMail_sender_from());
        helper.setSubject("Verification of your email address");
        // TODO HTML inline is not a good practice
        helper.setText("Dear Sir or Madam </p></p></p> In order to verify your email address, please open the following link: </p> <p><a \n"
                + "href=\"" + mailConfig.getMail_verification_server_url() + "/users/" + user.getId() + "/verify/" + user.getEmailVerifiedUrl() + "\">Click</a></p>\n"
                + "\n"
                + "<p>Best regards</p>\n"
                + "\n"
                + "<p>Coatrack Team</p>", true);
        return message;
    }

}
