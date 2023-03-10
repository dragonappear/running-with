package com.runningwith.domain.users;

import com.runningwith.domain.users.factory.UsersEntityFactory;
import com.runningwith.infra.MockMvcTest;
import com.runningwith.infra.mail.EmailMessage;
import com.runningwith.infra.mail.EmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static com.runningwith.domain.main.ExceptionAdvice.VIEW_ERROR;
import static com.runningwith.domain.users.UsersController.*;
import static com.runningwith.domain.users.WithUserSecurityContextFactory.EMAIL;
import static com.runningwith.infra.utils.CustomStringUtils.RANDOM_STRING;
import static com.runningwith.infra.utils.CustomStringUtils.WITH_USER_NICKNAME;
import static com.runningwith.infra.utils.WebUtils.REDIRECT;
import static com.runningwith.infra.utils.WebUtils.URL_REDIRECT_ROOT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@MockMvcTest
class UsersControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    UsersRepository usersRepository;

    @Autowired
    UsersService usersService;

    @Autowired
    UsersEntityFactory usersEntityFactory;

    @MockBean
    EmailService emailService;

    @BeforeEach
    void setUp() {
        usersEntityFactory.createUsersEntity("nickname");
    }

    @AfterEach
    void tearDown() {
        usersRepository.deleteAll();
    }

    @DisplayName("???????????? ??? - ??????")
    @Test
    void view_signUp() throws Exception {
        mockMvc.perform(get(URL_SIGN_UP))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(FORM_SIGN_UP))
                .andExpect(view().name(PAGE_SIGN_UP))
                .andExpect(unauthenticated());
    }

    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    void signUp_submit_with_correct_input() throws Exception {
        mockMvc.perform(post(URL_SIGN_UP)
                        .param("nickname", "tttttt")
                        .param("email", "email@email.com")
                        .param("password", "goodpassword")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(URL_REDIRECT_ROOT));

        Optional<UsersEntity> optionalUsersEntity = usersRepository.findByEmail("email@email.com");
        assertThat(optionalUsersEntity.isPresent()).isTrue();
        UsersEntity usersEntity = optionalUsersEntity.get();
        assertThat(usersEntity.getPassword()).isNotEqualTo("goodpassword");
        then(emailService).should().sendEmail(any(EmailMessage.class));
    }

    @DisplayName("???????????? ?????? - ????????? ??????")
    @Test
    void signUp_submit_with_wrong_input() throws Exception {
        mockMvc.perform(post(URL_SIGN_UP)
                        .param("nickname", "test")
                        .param("email", "email")
                        .param("password", "wrongpw")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().hasErrors())
                .andExpect(view().name(PAGE_SIGN_UP));
    }

    @WithUser
    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    @Test
    void check_email_token_with_correct_input() throws Exception {

        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();

        mockMvc.perform(get(URL_CHECK_EMAIL_TOKEN)
                        .param("token", usersEntity.getEmailCheckToken())
                        .param("email", usersEntity.getEmail()))
                .andExpect(status().isOk())
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(model().attributeExists("nickname"))
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_CHECKED_EMAIL));
    }

    @DisplayName("?????? ?????? ?????? - ????????? ??????")
    @Test
    void check_email_token_with_wrong_input() throws Exception {
        mockMvc.perform(get(URL_CHECK_EMAIL_TOKEN)
                        .param("token", "wrong-token")
                        .param("email", "email@email.com"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attributeDoesNotExist("nickname"))
                .andExpect(unauthenticated())
                .andExpect(view().name(PAGE_CHECKED_EMAIL));
    }

    @WithUser
    @DisplayName("?????? ?????? ??? - ??????")
    @Test
    void view_check_email() throws Exception {
        mockMvc.perform(get(URL_CHECK_EMAIL))
                .andExpect(model().attribute("email", WITH_USER_NICKNAME + EMAIL))
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_CHECK_EMAIL));
    }

    @WithUser
    @DisplayName("????????? ?????? - 1?????? ?????? ?????? ??????")
    @Test
    void resend_confirm_email_within_one_hour() throws Exception {
        mockMvc.perform(get(URL_RESEND_CONFIRM_EMAIL))
                .andExpect(model().attribute("error", "?????? ???????????? 1????????? ????????? ????????? ??? ????????????."))
                .andExpect(model().attribute("email", WITH_USER_NICKNAME + EMAIL))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_CHECK_EMAIL));
    }

    @WithUser
    @DisplayName("????????? ??? ?????? - by self")
    @Test
    void view_profile_success_by_self() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname(WITH_USER_NICKNAME).get();
        mockMvc.perform(get(URL_USERS_PROFILE + "/" + WITH_USER_NICKNAME))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("isOwner", true))
                .andExpect(status().isOk())
                .andExpect(authenticated())
                .andExpect(view().name(PAGE_USERS_PROFILE));
    }

    @DisplayName("????????? ??? ?????? - by other")
    @Test
    void view_profile_success_by_other() throws Exception {
        UsersEntity usersEntity = usersRepository.findByNickname("nickname").get();
        mockMvc.perform(get(URL_USERS_PROFILE + "/" + "nickname"))
                .andExpect(model().attribute("user", usersEntity))
                .andExpect(model().attribute("isOwner", false))
                .andExpect(status().isOk())
                .andExpect(unauthenticated())
                .andExpect(view().name(PAGE_USERS_PROFILE));
    }


    @DisplayName("????????? ??? ?????? - ?????? ??????")
    @Test
    void view_profile_failure_wrong_path() throws Exception {
        mockMvc.perform(get(URL_USERS_PROFILE + "/" + UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("error"))
                .andExpect(result -> {
                    Throwable ex = result.getResolvedException();
                    assertNotNull(ex);
                    assertEquals(IllegalArgumentException.class, ex.getClass());
                })
                .andExpect(view().name(VIEW_ERROR));
    }

    @DisplayName("????????? ????????? ?????? ???")
    @Test
    void view_email_login_link() throws Exception {
        mockMvc.perform(get(URL_EMAIL_LOGIN))
                .andExpect(unauthenticated())
                .andExpect(view().name(VIEW_EMAIL_LOGIN));
    }

    @DisplayName("????????? ????????? ?????? ??????- ????????? ??????")
    @Test
    void send_email_login_link_with_correct_inputs() throws Exception {
        mockMvc.perform(post(URL_EMAIL_LOGIN)
                        .param("email", "nickname" + EMAIL)
                        .with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name(REDIRECT + URL_EMAIL_LOGIN));
    }

    @DisplayName("????????? ????????? ?????? ??????- ????????? ??????")
    @Test
    void send_email_login_link_with_wrong_inputs() throws Exception {
        mockMvc.perform(post(URL_EMAIL_LOGIN)
                        .param("email", "1" + EMAIL)
                        .with(csrf()))
                .andExpect(unauthenticated())
                .andExpect(model().attribute("error", "????????? ????????? ????????? ????????????."))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_EMAIL_LOGIN));
    }

    @DisplayName("????????? ????????? - ????????? ??????")
    @Test
    void login_with_correct_email() throws Exception {
        UsersEntity usersEntity = usersRepository.findByEmail("nickname" + EMAIL).get();
        mockMvc.perform(get(URL_LOGIN_BY_EMAIL)
                        .queryParam("email", "nickname" + EMAIL)
                        .queryParam("token", usersEntity.getEmailCheckToken()))
                .andExpect(authenticated().withUsername("nickname"))
                .andExpect(model().attributeDoesNotExist("error"))
                .andExpect(status().isOk())
                .andExpect(view().name(VIEW_USERS_LOGGED_IN_BY_EMAIL));
    }

    @DisplayName("????????? ????????? - ????????? ??????")
    @Test
    void login_with_wrong_email() throws Exception {
        mockMvc.perform(get(URL_LOGIN_BY_EMAIL)
                        .queryParam("email", "1" + EMAIL)
                        .queryParam("token", RANDOM_STRING))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "?????? ????????? ???????????? ??? ????????????."))
                .andExpect(view().name(VIEW_USERS_LOGGED_IN_BY_EMAIL));
    }

    @DisplayName("????????? ????????? - ?????? ??????")
    @Test
    void login_with_wrong_token() throws Exception {
        mockMvc.perform(get(URL_LOGIN_BY_EMAIL)
                        .queryParam("email", "nickname" + EMAIL)
                        .queryParam("token", "token"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("error", "?????? ????????? ???????????? ??? ????????????."))
                .andExpect(view().name(VIEW_USERS_LOGGED_IN_BY_EMAIL));
    }


}