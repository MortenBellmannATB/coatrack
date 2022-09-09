package eu.coatrack.admin.user;

import eu.coatrack.admin.config.TestConfiguration;
import eu.coatrack.admin.controllers.UserController;
import eu.coatrack.admin.model.repository.UserRepository;
import eu.coatrack.admin.service.user.UserService;
import eu.coatrack.api.User;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Optional;

import static eu.coatrack.admin.report.ReportDataFactory.consumer;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static eu.coatrack.admin.user.UserDataFactory.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ContextConfiguration(classes = TestConfiguration.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    private final UserController userController;

    private final UserService userService;
    private final MockMvc mvc;
    private final static String basePath = "";

    public UserControllerTest() {
        userService = mock(UserService.class);

        doReturn(user).when(userService).findById(anyLong());

        userController = new UserController(userService);

        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ADMIN");
        Authentication authentication = new UsernamePasswordAuthenticationToken(consumer.getUsername(), "PetesPassword", Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(authentication);


        mvc = MockMvcBuilders.standaloneSetup(userController).build();
    }

    @Test
    public void userEmailVeritification() throws Exception {
        String query = String.format("%s/users/%d/verify/%s",
                basePath,
                user.getId(),
                emailVerificationCode
        );

        mvc.perform(get(query))
                .andExpect(status().isOk())
                .andExpect(view().name("verified"));
    }
}
