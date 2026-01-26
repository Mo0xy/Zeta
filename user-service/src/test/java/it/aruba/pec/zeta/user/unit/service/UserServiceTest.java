package it.aruba.pec.zeta.user.unit.service;

import it.aruba.pec.zeta.common.dto.PagedResponse;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.common.exception.DuplicateResourceException;
import it.aruba.pec.zeta.common.exception.ResourceNotFoundException;
import it.aruba.pec.zeta.common.exception.ValidationException;
import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.mapper.UserMapper;
import it.aruba.pec.zeta.user.repository.UserRepository;
import it.aruba.pec.zeta.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .passwordHash("hashedPassword")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .build();
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.getUserById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("testuser");
            verify(userRepository).findById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("getUserByUsername")
    class GetUserByUsername {

        @Test
        @DisplayName("should return user when found by username")
        void shouldReturnUserWhenFoundByUsername() {
            // Given
            when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.getUserByUsername("testuser");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when username not found")
        void shouldThrowExceptionWhenUsernameNotFound() {
            // Given
            when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.getUserByUsername("nonexistent"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("should return paginated users")
        void shouldReturnPaginatedUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.findAll(pageable)).thenReturn(userPage);
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            // When
            PagedResponse<UserDTO> result = userService.getAllUsers(pageable);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getPage()).isEqualTo(0);
        }

        @Test
        @DisplayName("should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsers() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(userRepository.findAll(pageable)).thenReturn(emptyPage);

            // When
            PagedResponse<UserDTO> result = userService.getAllUsers(pageable);

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .username("newuser")
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .build();

            User newUser = User.builder()
                    .username("newuser")
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .build();

            User savedUser = User.builder()
                    .id(2L)
                    .username("newuser")
                    .email("new@example.com")
                    .firstName("New")
                    .lastName("User")
                    .passwordHash("encodedPassword")
                    .enabled(true)
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(userMapper.toEntity(inputDTO)).thenReturn(newUser);
            when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toDTO(savedUser)).thenReturn(inputDTO);

            // When
            UserDTO result = userService.createUser(inputDTO, "password123");

            // Then
            assertThat(result).isNotNull();
            verify(userRepository).save(any(User.class));
            verify(passwordEncoder).encode("password123");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when username exists")
        void shouldThrowExceptionWhenUsernameExists() {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .username("existinguser")
                    .email("new@example.com")
                    .build();

            when(userRepository.existsByUsername("existinguser")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(inputDTO, "password"))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("username");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when email exists")
        void shouldThrowExceptionWhenEmailExists() {
            // Given
            UserDTO inputDTO = UserDTO.builder()
                    .username("newuser")
                    .email("existing@example.com")
                    .build();

            when(userRepository.existsByUsername("newuser")).thenReturn(false);
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.createUser(inputDTO, "password"))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("email");
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() {
            // Given
            UserDTO updateDTO = UserDTO.builder()
                    .firstName("Updated")
                    .lastName("Name")
                    .build();

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenReturn(testUser);
            when(userMapper.toDTO(any(User.class))).thenReturn(testUserDTO);

            // When
            UserDTO result = userService.updateUser(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            verify(userMapper).updateEntityFromDTO(updateDTO, testUser);
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            // Given
            UserDTO updateDTO = UserDTO.builder().firstName("Updated").build();
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> userService.updateUser(999L, updateDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("changePassword")
    class ChangePassword {

        @Test
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("newHashedPassword");

            // When
            userService.changePassword(1L, "oldPassword", "newPassword123");

            // Then
            verify(userRepository).save(testUser);
            assertThat(testUser.getPasswordHash()).isEqualTo("newHashedPassword");
        }

        @Test
        @DisplayName("should throw ValidationException when old password is incorrect")
        void shouldThrowExceptionWhenOldPasswordIncorrect() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("wrongPassword", "hashedPassword")).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "newPassword"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Password attuale non corretta");
        }

        @Test
        @DisplayName("should throw ValidationException when new password is too short")
        void shouldThrowExceptionWhenNewPasswordTooShort() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(passwordEncoder.matches("oldPassword", "hashedPassword")).thenReturn(true);

            // When/Then
            assertThatThrownBy(() -> userService.changePassword(1L, "oldPassword", "short"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("almeno 8 caratteri");
        }
    }

    @Nested
    @DisplayName("enableUser/disableUser")
    class EnableDisableUser {

        @Test
        @DisplayName("should enable user successfully")
        void shouldEnableUserSuccessfully() {
            // Given
            testUser.setEnabled(false);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.enableUser(1L);

            // Then
            assertThat(testUser.isEnabled()).isTrue();
            verify(userRepository).save(testUser);
        }

        @Test
        @DisplayName("should disable user successfully")
        void shouldDisableUserSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            // When
            userService.disableUser(1L);

            // Then
            assertThat(testUser.isEnabled()).isFalse();
            verify(userRepository).save(testUser);
        }
    }

    @Nested
    @DisplayName("deleteUser")
    class DeleteUser {

        @Test
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() {
            // Given
            when(userRepository.existsById(1L)).thenReturn(true);

            // When
            userService.deleteUser(1L);

            // Then
            verify(userRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when user not found for deletion")
        void shouldThrowExceptionWhenUserNotFoundForDeletion() {
            // Given
            when(userRepository.existsById(999L)).thenReturn(false);

            // When/Then
            assertThatThrownBy(() -> userService.deleteUser(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("searchUsers")
    class SearchUsers {

        @Test
        @DisplayName("should search users by query")
        void shouldSearchUsersByQuery() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

            when(userRepository.searchUsers("test", pageable)).thenReturn(userPage);
            when(userMapper.toDTO(testUser)).thenReturn(testUserDTO);

            // When
            PagedResponse<UserDTO> result = userService.searchUsers("test", pageable);

            // Then
            assertThat(result.getContent()).hasSize(1);
            verify(userRepository).searchUsers("test", pageable);
        }
    }
}