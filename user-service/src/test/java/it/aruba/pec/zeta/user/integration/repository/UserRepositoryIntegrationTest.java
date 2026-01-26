package it.aruba.pec.zeta.user.integration.repository;

import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

// Spring Boot 4.x imports
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")  // Usa H2 definito in application-test.yml
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("findByUsername")
    class FindByUsername {

        @Test
        @DisplayName("should find user by username")
        void shouldFindUserByUsername() {
            entityManager.persistAndFlush(testUser);

            Optional<User> found = userRepository.findByUsername("testuser");

            assertThat(found).isPresent();
            assertThat(found.get().getUsername()).isEqualTo("testuser");
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should return empty when username not found")
        void shouldReturnEmptyWhenUsernameNotFound() {
            Optional<User> found = userRepository.findByUsername("nonexistent");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmail {

        @Test
        @DisplayName("should find user by email")
        void shouldFindUserByEmail() {
            entityManager.persistAndFlush(testUser);

            Optional<User> found = userRepository.findByEmail("test@example.com");

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("existsByUsername/existsByEmail")
    class ExistsMethods {

        @Test
        @DisplayName("should return true when username exists")
        void shouldReturnTrueWhenUsernameExists() {
            entityManager.persistAndFlush(testUser);

            assertThat(userRepository.existsByUsername("testuser")).isTrue();
            assertThat(userRepository.existsByUsername("nonexistent")).isFalse();
        }

        @Test
        @DisplayName("should return true when email exists")
        void shouldReturnTrueWhenEmailExists() {
            entityManager.persistAndFlush(testUser);

            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
            assertThat(userRepository.existsByEmail("nonexistent@example.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("findByEnabledTrue")
    class FindByEnabledTrue {

        @Test
        @DisplayName("should find only enabled users")
        void shouldFindOnlyEnabledUsers() {
            User enabledUser = User.builder()
                    .username("enabled")
                    .email("enabled@example.com")
                    .passwordHash("hash")
                    .firstName("Enabled")
                    .lastName("User")
                    .enabled(true)
                    .build();

            User disabledUser = User.builder()
                    .username("disabled")
                    .email("disabled@example.com")
                    .passwordHash("hash")
                    .firstName("Disabled")
                    .lastName("User")
                    .enabled(false)
                    .build();

            entityManager.persistAndFlush(enabledUser);
            entityManager.persistAndFlush(disabledUser);

            Page<User> enabledUsers = userRepository.findByEnabledTrue(PageRequest.of(0, 20));

            // Tutti i risultati devono essere enabled = true
            assertThat(enabledUsers.getContent())
                    .as("all returned users should be enabled")
                    .allMatch(User::isEnabled);

            // Deve contenere l'utente 'enabled' e NON contenere 'disabled'
            assertThat(enabledUsers.getContent())
                    .extracting(User::getUsername)
                    .contains("enabled")
                    .doesNotContain("disabled");
        }
    }

    @Nested
    @DisplayName("searchUsers")
    class SearchUsers {

        @Test
        @DisplayName("should search by username")
        void shouldSearchByUsername() {
            entityManager.persistAndFlush(testUser);

            User anotherUser = User.builder()
                    .username("anotheruser")
                    .email("another@example.com")
                    .passwordHash("hash")
                    .firstName("Another")
                    .lastName("Person")
                    .enabled(true)
                    .build();
            entityManager.persistAndFlush(anotherUser);

            // Usiamo una query abbastanza specifica da NON matchare gli utenti di data.sql
            Page<User> results = userRepository.searchUsers("testuser", PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should be case insensitive")
        void shouldBeCaseInsensitive() {
            entityManager.persistAndFlush(testUser);

            // Stessa logica, ma con maiuscole per testare case-insensitive
            Page<User> results = userRepository.searchUsers("TESTUSER", PageRequest.of(0, 10));

            assertThat(results.getContent()).hasSize(1);
            assertThat(results.getContent().get(0).getUsername()).isEqualTo("testuser");
        }
    }
}
