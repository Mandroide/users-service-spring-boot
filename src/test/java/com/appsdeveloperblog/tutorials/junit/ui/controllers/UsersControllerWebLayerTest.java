package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.service.UsersService;
import com.appsdeveloperblog.tutorials.junit.shared.UserDto;
import com.appsdeveloperblog.tutorials.junit.ui.request.UserDetailsRequestModel;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@WebMvcTest(value = UsersController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
//@MockBean({UsersServiceImpl.class})
//@AutoConfigureMockMvc(addFilters = false)
class UsersControllerWebLayerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    //@Autowired //-- Only if you specify MockBean at class level
    private UsersService usersService;
    private UserDetailsRequestModel userDetailsRequestModel;

    @BeforeEach
    void setUp() {
        userDetailsRequestModel = new UserDetailsRequestModel();
        userDetailsRequestModel.setFirstName("Sergey");
        userDetailsRequestModel.setLastName("Kargopolov");
        userDetailsRequestModel.setEmail("email@test.com");
        userDetailsRequestModel.setPassword("12345678");
        userDetailsRequestModel.setRepeatPassword("12345678");
    }

    @Test
    @DisplayName("User can be created")
    void createUser_WhenValidUserDetailsProvided_ReturnsCreatedUserDetails() throws Exception {

//        var userDto = new UserDto();
//        userDto.setFirstName(firstName);
//        userDto.setLastName(lastName);
//        userDto.setEmail(email);
//        userDto.setUserId(UUID.randomUUID().toString());

        var userDto = new ModelMapper().map(userDetailsRequestModel, UserDto.class);
        userDto.setUserId(UUID.randomUUID().toString());
        when(usersService.createUser(any(UserDto.class))).thenReturn(userDto);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        MvcResult mvcResult = mockMvc.perform(requestBuilder).andReturn();
        String responseBodyAsString = mvcResult.getResponse().getContentAsString();
        var userRest = new ObjectMapper().readValue(responseBodyAsString, UserRest.class);

        // Assert
        assertAll(() -> assertEquals(userDetailsRequestModel.getFirstName(), userRest.getFirstName()),
                () -> assertEquals(userDetailsRequestModel.getLastName(), userRest.getLastName()),
                () -> assertEquals(userDetailsRequestModel.getEmail(), userRest.getEmail()),
                () -> assertNotNull(userRest.getUserId()));
    }

    @CsvFileSource(resources = "/users_controller_create_user_bad_request.csv", useHeadersInDisplayName = true)
    @ParameterizedTest
    @DisplayName("User property is invalid")
    void createUser_WhenPropertyIsInvalid_Returns400StatusCode(String firstName, String lastName,
                                                                    String email, String password,
                                                                    String repeatPassword) throws Exception {
        // Arrange
        userDetailsRequestModel.setFirstName(firstName);
        userDetailsRequestModel.setLastName(lastName);
        userDetailsRequestModel.setEmail(email);
        userDetailsRequestModel.setPassword(password);
        userDetailsRequestModel.setRepeatPassword(repeatPassword);

        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userDetailsRequestModel));

        // Act
        var mvcResult = mockMvc.perform(requestBuilder).andReturn();

        // Assert
        Assertions.assertEquals(HttpStatus.BAD_REQUEST.value(), mvcResult.getResponse().getStatus(),
                "Incorrect HTTP Status Code returned");
    }

}