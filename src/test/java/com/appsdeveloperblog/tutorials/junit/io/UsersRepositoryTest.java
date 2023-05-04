package com.appsdeveloperblog.tutorials.junit.io;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsersRepositoryTest {
    @Autowired
    TestEntityManager testEntityManager;
    @Autowired
    UsersRepository usersRepository;
    String userId1 = "userId";
    String email1 = "test@test.com";
    String userId2 = "userId2";
    String email2 = "test2@test.com";
    @BeforeEach
    void setUp() {
        UserEntity entity = new UserEntity();
        entity.setFirstName("Sergey");
        entity.setLastName("Kargopolov");
        entity.setEmail(email1);
        entity.setUserId(userId1);
        entity.setEncryptedPassword("123456789");
        testEntityManager.persistAndFlush(entity);

        UserEntity entity2 = new UserEntity();
        entity2.setFirstName("John");
        entity2.setLastName("Sears");
        entity2.setEmail(email2);
        entity2.setUserId(userId2);
        entity2.setEncryptedPassword("abcdefg1");
        testEntityManager.persistAndFlush(entity2);
    }

    @Test
    void findByEmail_WhenCorrectEmail_ReturnUserEntity() {
        // Act
        UserEntity result = usersRepository.findByEmail(email1);

        // Assert
        Assertions.assertEquals(email1, result.getEmail());
    }

    @Test
    void findByUserId_WhenCorrectUserId_ReturnUserEntity() {
        // Act
        UserEntity result = usersRepository.findByUserId(userId2);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(userId2, result.getUserId());
    }

    @Test
    void findUsersWithEmailEndsWith_WhenGivenEmailDomain_ReturnsUsersWithGivenDomain() {
        UserEntity entity = new UserEntity();
        entity.setUserId("userId3");
        entity.setFirstName("Sergey");
        entity.setLastName("Kargopolov");
        entity.setEmail("test@gmail.com");
        entity.setEncryptedPassword("123456789");
        UserEntity result = testEntityManager.persistAndFlush(entity);

        String emailDomain = "@gmail.com";

        List<UserEntity> userEntities = usersRepository.findUsersWithEmailEndingWith(emailDomain);

        int expectedSize = 1;
        Assertions.assertEquals(expectedSize, userEntities.size());
        Assertions.assertTrue(userEntities.get(0).getEmail().endsWith(emailDomain));
    }
}