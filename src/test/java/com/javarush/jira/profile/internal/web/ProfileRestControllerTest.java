package com.javarush.jira.profile.internal.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.jira.AbstractControllerTest;
import com.javarush.jira.profile.ContactTo;
import com.javarush.jira.profile.ProfileTo;
import com.javarush.jira.profile.internal.ProfileMapper;
import com.javarush.jira.profile.internal.ProfileRepository;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Set;

import static com.javarush.jira.login.internal.web.UserTestData.ADMIN_MAIL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ProfileRestControllerTest extends AbstractControllerTest {

    public static final String REST_URL = "/api/profile";

    private final ProfileTo TestProfileNoContent = new ProfileTo(null,
            Set.of("deadline", "assigned"),
            Set.of(new ContactTo("skype", "mySkype"),
                    new ContactTo("mobile", "+380964536066"),
                    new ContactTo("website", "helloTest.com")));

    @Autowired
    protected ProfileMapper profileMapper;
    @Autowired
    private ProfileRepository profileRepository;
    @Autowired
    private ObjectMapper mapper;

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    public void SuccessAuthProfile() throws Exception {
        ResultActions response = perform(get(REST_URL));
        response.andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id", CoreMatchers.is(2)));
    }

    @Test
    public void ErrorAuthProfile() throws Exception {
        ResultActions response = perform(get(REST_URL));
        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    public void SuccessUpdateProfile() throws Exception {
        ResultActions response = perform(put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(TestProfileNoContent)));

        response.andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @WithUserDetails(value = ADMIN_MAIL)
    public void ErrorUpdateBecauseNotFoundProfileInRequestBody() throws Exception {
        ResultActions response = perform(put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON));

        response.andDo(print())
                .andExpect(status().isInternalServerError());
    }

    @Test
    public void ErrorUpdateBecauseUserNotAuth() throws Exception {
        ResultActions response = perform(put(REST_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(TestProfileNoContent)));

        response.andDo(print())
                .andExpect(status().isUnauthorized());
    }
}