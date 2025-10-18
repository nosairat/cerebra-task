package sa.cerebra.task.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import sa.cerebra.task.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = new User();
        testUser.setPhone("+1234567890");
        
        // Clear any existing data
        userRepository.deleteAll();
    }

    @Test
    void findByPhone_ShouldReturnUser_WhenUserExists() {
        // Given
        entityManager.persistAndFlush(testUser);

        // When
        Optional<User> result = userRepository.findByPhone("+1234567890");

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("+1234567890", result.get().getPhone());
    }

    @Test
    void findByPhone_ShouldReturnEmptyOptional_WhenUserDoesNotExist() {
        // Given - no user persisted

        // When
        Optional<User> result = userRepository.findByPhone("+9876543210");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByPhone_ShouldReturnEmptyOptional_WhenPhoneIsNull() {
        // Given - no user persisted

        // When
        Optional<User> result = userRepository.findByPhone(null);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByPhone_ShouldReturnEmptyOptional_WhenPhoneIsEmpty() {
        // Given - no user persisted

        // When
        Optional<User> result = userRepository.findByPhone("");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByPhone_ShouldReturnEmptyOptional_WhenPhoneIsBlank() {
        // Given - no user persisted

        // When
        Optional<User> result = userRepository.findByPhone("   ");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void findByPhone_ShouldReturnCorrectUser_WhenMultipleUsersExist() {
        // Given
        User user1 = new User();
        user1.setPhone("+1111111111");
        
        User user2 = new User();
        user2.setPhone("+2222222222");
        
        User user3 = new User();
        user3.setPhone("+3333333333");
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // When
        Optional<User> result = userRepository.findByPhone("+2222222222");

        // Then
        assertTrue(result.isPresent());
        assertEquals(user2.getId(), result.get().getId());
        assertEquals("+2222222222", result.get().getPhone());
    }



    @Test
    void findByPhone_ShouldHandleSpecialCharacters_WhenPhoneContainsSpecialChars() {
        // Given
        User user1 = new User();
        user1.setPhone("+1-234-567-8900");
        
        User user2 = new User();
        user2.setPhone("+1 (234) 567-8900");
        
        User user3 = new User();
        user3.setPhone("+1.234.567.8900");
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // When
        Optional<User> result1 = userRepository.findByPhone("+1-234-567-8900");
        Optional<User> result2 = userRepository.findByPhone("+1 (234) 567-8900");
        Optional<User> result3 = userRepository.findByPhone("+1.234.567.8900");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertEquals("+1-234-567-8900", result1.get().getPhone());
        assertEquals("+1 (234) 567-8900", result2.get().getPhone());
        assertEquals("+1.234.567.8900", result3.get().getPhone());
    }

    @Test
    void findByPhone_ShouldHandleInternationalNumbers_WhenPhoneHasCountryCode() {
        // Given
        User user1 = new User();
        user1.setPhone("+966501234567"); // Saudi Arabia
        
        User user2 = new User();
        user2.setPhone("+971501234567"); // UAE
        
        User user3 = new User();
        user3.setPhone("+201234567890"); // Egypt
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);
        entityManager.persistAndFlush(user3);

        // When
        Optional<User> result1 = userRepository.findByPhone("+966501234567");
        Optional<User> result2 = userRepository.findByPhone("+971501234567");
        Optional<User> result3 = userRepository.findByPhone("+201234567890");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertTrue(result3.isPresent());
        assertEquals("+966501234567", result1.get().getPhone());
        assertEquals("+971501234567", result2.get().getPhone());
        assertEquals("+201234567890", result3.get().getPhone());
    }

    @Test
    void findByPhone_ShouldHandleLongPhoneNumbers_WhenPhoneIsVeryLong() {
        // Given
        String longPhone = "+123456789012345678901234567890";
        User user = new User();
        user.setPhone(longPhone);
        
        entityManager.persistAndFlush(user);

        // When
        Optional<User> result = userRepository.findByPhone(longPhone);

        // Then
        assertTrue(result.isPresent());
        assertEquals(longPhone, result.get().getPhone());
    }

    @Test
    void save_ShouldPersistUser_WhenValidUserProvided() {
        // Given
        User newUser = new User();
        newUser.setPhone("+9999999999");

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertNotNull(savedUser.getId());
        assertEquals("+9999999999", savedUser.getPhone());
        
        // Verify it can be retrieved
        Optional<User> retrievedUser = userRepository.findByPhone("+9999999999");
        assertTrue(retrievedUser.isPresent());
        assertEquals(savedUser.getId(), retrievedUser.get().getId());
    }

    @Test
    void save_ShouldUpdateUser_WhenExistingUserModified() {
        // Given
        User user = new User();
        user.setPhone("+1111111111");
        User savedUser = userRepository.save(user);
        
        // When
        savedUser.setPhone("+2222222222");
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("+2222222222", updatedUser.getPhone());
        
        // Verify old phone number is not found
        Optional<User> oldPhoneResult = userRepository.findByPhone("+1111111111");
        assertFalse(oldPhoneResult.isPresent());
        
        // Verify new phone number is found
        Optional<User> newPhoneResult = userRepository.findByPhone("+2222222222");
        assertTrue(newPhoneResult.isPresent());
        assertEquals(updatedUser.getId(), newPhoneResult.get().getId());
    }

    @Test
    void delete_ShouldRemoveUser_WhenUserDeleted() {
        // Given
        User user = new User();
        user.setPhone("+5555555555");
        User savedUser = userRepository.save(user);
        
        // Verify user exists
        Optional<User> beforeDelete = userRepository.findByPhone("+5555555555");
        assertTrue(beforeDelete.isPresent());

        // When
        userRepository.delete(savedUser);

        // Then
        Optional<User> afterDelete = userRepository.findByPhone("+5555555555");
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void deleteById_ShouldRemoveUser_WhenUserDeletedById() {
        // Given
        User user = new User();
        user.setPhone("+6666666666");
        User savedUser = userRepository.save(user);
        
        // Verify user exists
        Optional<User> beforeDelete = userRepository.findByPhone("+6666666666");
        assertTrue(beforeDelete.isPresent());

        // When
        userRepository.deleteById(savedUser.getId());

        // Then
        Optional<User> afterDelete = userRepository.findByPhone("+6666666666");
        assertFalse(afterDelete.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers_WhenMultipleUsersExist() {
        // Given
        User user1 = new User();
        user1.setPhone("+1111111111");
        
        User user2 = new User();
        user2.setPhone("+2222222222");
        
        User user3 = new User();
        user3.setPhone("+3333333333");
        
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);

        // When
        var allUsers = userRepository.findAll();

        // Then
        assertEquals(3, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> "+1111111111".equals(u.getPhone())));
        assertTrue(allUsers.stream().anyMatch(u -> "+2222222222".equals(u.getPhone())));
        assertTrue(allUsers.stream().anyMatch(u -> "+3333333333".equals(u.getPhone())));
    }

    @Test
    void count_ShouldReturnCorrectCount_WhenUsersExist() {
        // Given
        User user1 = new User();
        user1.setPhone("+1111111111");
        
        User user2 = new User();
        user2.setPhone("+2222222222");
        
        userRepository.save(user1);
        userRepository.save(user2);

        // When
        long count = userRepository.count();

        // Then
        assertEquals(2, count);
    }

    @Test
    void existsById_ShouldReturnTrue_WhenUserExists() {
        // Given
        User user = new User();
        user.setPhone("+7777777777");
        User savedUser = userRepository.save(user);

        // When
        boolean exists = userRepository.existsById(savedUser.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void existsById_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Given - no user with ID 99999

        // When
        boolean exists = userRepository.existsById(99999L);

        // Then
        assertFalse(exists);
    }

    @Test
    void findByPhone_ShouldHandleUnicodeCharacters_WhenPhoneContainsUnicode() {
        // Given
        String unicodePhone = "+1234567890\u0660\u0661\u0662"; // Contains Arabic-Indic digits
        User user = new User();
        user.setPhone(unicodePhone);
        
        entityManager.persistAndFlush(user);

        // When
        Optional<User> result = userRepository.findByPhone(unicodePhone);

        // Then
        assertTrue(result.isPresent());
        assertEquals(unicodePhone, result.get().getPhone());
    }

    @Test
    void findByPhone_ShouldBeExactMatch_WhenPhoneNumbersAreSimilar() {
        // Given
        User user1 = new User();
        user1.setPhone("+1234567890");
        
        User user2 = new User();
        user2.setPhone("+1234567891"); // Only last digit differs
        
        entityManager.persistAndFlush(user1);
        entityManager.persistAndFlush(user2);

        // When
        Optional<User> result1 = userRepository.findByPhone("+1234567890");
        Optional<User> result2 = userRepository.findByPhone("+1234567891");

        // Then
        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertNotEquals(result1.get().getId(), result2.get().getId());
        assertEquals("+1234567890", result1.get().getPhone());
        assertEquals("+1234567891", result2.get().getPhone());
    }
}
