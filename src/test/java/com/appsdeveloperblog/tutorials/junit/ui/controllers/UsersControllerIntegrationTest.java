package com.appsdeveloperblog.tutorials.junit.ui.controllers;

import com.appsdeveloperblog.tutorials.junit.security.SecurityConstants;
import com.appsdeveloperblog.tutorials.junit.ui.response.UserRest;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//@TestPropertySource(locations = "/application-test.properties", properties = "server.port=8083")
//,properties = "server.port=8081")
public class UsersControllerIntegrationTest {
    @Value("${server.port}")
    private int serverPort;
    @LocalServerPort
    private int localServerPort;
    @Autowired
    private TestRestTemplate restTemplate;
    private String authorizationHeader;
//    @Test
//    void contextLoads() {
//        System.out.printf("server.port=%d%n", serverPort);
//        System.out.printf("local server port=%d%n", localServerPort);
//    }

    @DisplayName("User can be created")
    @Test
    @Order(1)
    void createUser_WhenValidDetailsProvided_ReturnsUserDetails() throws JSONException {
        // Arrange
        var userDetailsRequestJson = new JSONObject();
        userDetailsRequestJson.put("firstName", "Sergey");
        userDetailsRequestJson.put("lastName", "Kargopolov");
        userDetailsRequestJson.put("email", "test@test.com");
        userDetailsRequestJson.put("password", "12345678");
        userDetailsRequestJson.put("repeatPassword", "12345678");
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        var httpEntity = new HttpEntity<>(userDetailsRequestJson.toString(), httpHeaders);

        // Act
        ResponseEntity<UserRest> userRestResponseEntity = restTemplate.postForEntity("/users", httpEntity, UserRest.class);
        UserRest body = userRestResponseEntity.getBody();

        // Assert
        assertNotNull(body);
        assertAll(() -> assertEquals(HttpStatus.OK, userRestResponseEntity.getStatusCode()),
                () -> assertEquals(userDetailsRequestJson.getString("firstName"), body.getFirstName()),
                () -> assertEquals(userDetailsRequestJson.getString("lastName"), body.getLastName()),
                () -> assertEquals(userDetailsRequestJson.getString("email"), body.getEmail()),
                () -> assertNotNull(body.getUserId())
        );
    }

    @DisplayName("GET /users requires JWT")
    @Order(2)
    @Test
    void getUsers_WhenMissingJWT_Returns403() {
        // Arrange
        var httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        // Act
        ResponseEntity<List<UserRest>> responseEntity = restTemplate.exchange("/users", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<List<UserRest>>() {
                });

        // Assert
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
    }

    @DisplayName("/login works")
    @Order(3)
    @Test
    void userLogin_WhenValidCredentials_ReturnsJWTInAuthorizationHeader() throws JSONException {
        // Arrange
        var jsonObject = new JSONObject()
                .put("email", "test@test.com")
                .put("password", "12345678");
        // Act
        HttpEntity<String> httpEntity = new HttpEntity<>(jsonObject.toString());
        ResponseEntity<Object> responseEntity = restTemplate.postForEntity("/users/login", httpEntity, null);
        authorizationHeader = responseEntity.getHeaders().getValuesAsList(SecurityConstants.HEADER_STRING).get(0);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(authorizationHeader);
        assertNotNull(responseEntity.getHeaders().getValuesAsList("UserID")
                .get(0));


    }

    @Order(4)
    @DisplayName("GET /users works")
    @Test
    void getUsers_whenValidJWTProvided_ReturnUsers() {
        // Arrange
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
        httpHeaders.setBearerAuth(authorizationHeader);

        // Act
        var httpEntity = new HttpEntity<>(httpHeaders);
        var responseEntity = restTemplate.exchange("/users", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<List<UserRest>>() {
        });
        List<UserRest> userRests = responseEntity.getBody();

        // Assert
        assertNotNull(userRests);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(1, userRests.size());
    }
}
