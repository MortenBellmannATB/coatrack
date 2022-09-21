package eu.coatrack.admin.controllers.mvc;

/*-
 * #%L
 * coatrack-admin
 * %%
 * Copyright (C) 2013 - 2020 Corizon | Institut für angewandte Systemtechnik Bremen GmbH (ATB)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import eu.coatrack.admin.service.user.UserService;
import eu.coatrack.api.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;

/**
 * @author gr-hovest(at)atb-bremen.de
 */
@AllArgsConstructor
@Slf4j
@Controller
@RequestMapping(value = "/")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/register")
    public String registerForm(User user, Model model) {
        if (user == null) {
            user = new User();
        }
        model.addAttribute("user", user);
        return "register";
    }

    @PostMapping(value = "/register")
    public String registerUser(User user, BindingResult bindingResult) {
        String redirectString = "redirect:/admin";

        if (bindingResult.hasErrors())
            redirectString = "register";
        else
            userService.registerUser(user, bindingResult);

        return redirectString;
    }

    @GetMapping(value = "users/{id}/verify/{emailVerificationCode}")
    public ModelAndView userEmailVeritification(@PathVariable("id") Long id, @PathVariable("emailVerificationCode") String emailVerificationCode) {
        userService.verifyUserViaVerificationCode(id, emailVerificationCode);
        return new ModelAndView("verified");
    }

    @RequestMapping(value = "/me", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public User me() {
        return userService.getAuthenticatedUser();
    }
}
