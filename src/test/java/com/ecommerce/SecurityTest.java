package com.ecommerce;

import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
    @Autowired MockMvc mvc;
    @Autowired UserService userService;

    @Test
    void catalogIsPublic() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/products")).andExpect(status().isOk());
        mvc.perform(get("/login")).andExpect(status().isOk());
    }

    @Test
    void cartAndOrdersNeedLogin() throws Exception {
        mvc.perform(get("/cart")).andExpect(status().is3xxRedirection()).andExpect(redirectedUrlPattern("**/login"));
        mvc.perform(get("/orders")).andExpect(status().is3xxRedirection());
        mvc.perform(get("/checkout")).andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminAreaIsForbiddenForNormalUsers() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isForbidden());
        mvc.perform(get("/admin/products")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminAreaOpensForAdmins() throws Exception {
        mvc.perform(get("/admin")).andExpect(status().isOk());
        mvc.perform(get("/admin/products")).andExpect(status().isOk());
        mvc.perform(get("/admin/orders")).andExpect(status().isOk());
        mvc.perform(get("/admin/users")).andExpect(status().isOk());
    }

    @Test
    void registrationValidatesInput() throws Exception {
        mvc.perform(post("/register").with(csrf()).param("email", "not-an-email").param("password", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("user", "firstName", "email", "password"));
    }

    @Test
    void registeredUserCanLogInAndWrongPasswordIsRejected() throws Exception {
        String email = UUID.randomUUID() + "@example.com";
        RegistrationDto dto = new RegistrationDto();
        dto.setFirstName("Log");
        dto.setLastName("In");
        dto.setEmail(email);
        dto.setPassword("secret1");
        userService.register(dto);

        mvc.perform(formLogin("/login").user("email", email).password("password", "secret1")).andExpect(authenticated());
        mvc.perform(formLogin("/login").user("email", email).password("password", "wrong")).andExpect(unauthenticated());
    }
}
