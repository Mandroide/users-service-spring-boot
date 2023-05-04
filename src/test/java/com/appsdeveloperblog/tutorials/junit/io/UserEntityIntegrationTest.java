package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import javax.persistence.PersistenceException;
import java.util.UUID;


@DataJpaTest
class UserEntityIntegrationTest {
    @Autowired
    private TestEntityManager testEntityManager;
    UserEntity userEntity;

    @BeforeEach
    void setUp() {
        userEntity = new UserEntity();
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setFirstName("Sergey");
        userEntity.setLastName("Kargopolov");
        userEntity.setEmail("test@test.com");
        userEntity.setEncryptedPassword("12345678");
    }

    @Test
    void testUserEntity_WhenValidUserDetailsProvided_ReturnStoredUserDetails() {
        // Arrange

        // Act
        UserEntity actual = testEntityManager.persistAndFlush(userEntity);

        // Assert
        Assertions.assertAll(() -> Assertions.assertTrue(actual.getId() > 0),
                () -> Assertions.assertEquals(userEntity.getFirstName(), actual.getFirstName()),
                () -> Assertions.assertEquals(userEntity.getLastName(), actual.getLastName()),
                () -> Assertions.assertEquals(userEntity.getEmail(), actual.getEmail()),
                () -> Assertions.assertEquals(userEntity.getEncryptedPassword(), actual.getEncryptedPassword()));
        
    }

    @Test
    void testUserEntity_WhenFirstNameIsTooLong_ExceptionIsThrown() {
        // Arrange
        userEntity.setFirstName("123456789012345678901234567890123456789012345678901234567890");

        // Assert & Act
        Assertions.assertThrows(PersistenceException.class, () -> {
            testEntityManager.persistAndFlush(userEntity);
        });
    }

    @Test
    void testUserEntity_WhenUserIdIsNotUnique_ExceptionIsThrown() {
        String userId = "userId";
        userEntity.setUserId(userId);

        UserEntity userEntity2 = new UserEntity();
        userEntity2.setUserId(userId);
        userEntity2.setFirstName(userEntity.getFirstName());
        userEntity2.setLastName(userEntity.getLastName());
        userEntity2.setEmail(userEntity.getEmail());
        userEntity2.setEncryptedPassword(userEntity.getEncryptedPassword());
        testEntityManager.persistAndFlush(userEntity);

        Assertions.assertThrows(PersistenceException.class, () -> testEntityManager.persistAndFlush(userEntity2));
    }
}