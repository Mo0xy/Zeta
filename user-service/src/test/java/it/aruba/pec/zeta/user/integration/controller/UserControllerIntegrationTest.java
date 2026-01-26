package it.aruba.pec.zeta.user.integration.controller;

import it.aruba.pec.zeta.common.dto.PagedResponse;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.common.exception.DuplicateResourceException;
import it.aruba.pec.zeta.common.exception.ResourceNotFoundException;
import it.aruba.pec.zeta.user.controller.UserController;
import it.aruba.pec.zeta.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "it\\.aruba\\.pec\\.zeta\\.user\\.security\\..*"
        )
)
@DisplayName("UserController Integration Tests")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private UserDTO testUserDTO;

    @BeforeEach
    void setUp() {
        testUserDTO = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @WithMockUser
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() throws Exception {
            // Given
            when(userService.getUserById(1L)).thenReturn(testUserDTO);

            // When/Then
            mockMvc.perform(get("/api/users/1"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.username").value("testuser"))
                    .andExpect(jsonPath("$.data.email").value("test@example.com"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            // Given
            when(userService.getUserById(999L))
                    .thenThrow(new ResourceNotFoundException("Utente", 999L));

            // When/Then
            mockMvc.perform(get("/api/users/999"))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @WithMockUser
        @DisplayName("should return paginated users")
        void shouldReturnPaginatedUsers() throws Exception {
            // Given
            PagedResponse<UserDTO> pagedResponse = PagedResponse.of(
                    List.of(testUserDTO),
                    0, 20, 1
            );
            when(userService.getAllUsers(any())).thenReturn(pagedResponse);

            // When/Then
            mockMvc.perform(get("/api/users")
                            .param("page", "0")
                            .param("size", "20"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/users/search")
    class SearchUsers {

        @Test
        @WithMockUser
        @DisplayName("should return search results")
        void shouldReturnSearchResults() throws Exception {
            // Given
            PagedResponse<UserDTO> pagedResponse = PagedResponse.of(
                    List.of(testUserDTO),
                    0, 20, 1
            );
            when(userService.searchUsers(eq("test"), any())).thenReturn(pagedResponse);

            // When/Then
            mockMvc.perform(get("/api/users/search")
                            .param("q", "test"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("GET /api/users/username/{username}")
    class GetUserByUsername {

        @Test
        @WithMockUser
        @DisplayName("should return user by username")
        void shouldReturnUserByUsername() throws Exception {
            // Given
            when(userService.getUserByUsername("testuser")).thenReturn(testUserDTO);

            // When/Then
            mockMvc.perform(get("/api/users/username/testuser"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.username").value("testuser"));
        }
    }

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @WithMockUser
        @DisplayName("should create user successfully")
        void shouldCreateUserSuccessfully() throws Exception {
            // Given
            UserController.CreateUserRequest request = new UserController.CreateUserRequest();
            request.setUser(testUserDTO);
            request.setPassword("password123");

            when(userService.createUser(any(UserDTO.class), eq("password123")))
                    .thenReturn(testUserDTO);

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Utente creato con successo"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 409 when username already exists")
        void shouldReturn409WhenUsernameExists() throws Exception {
            // Given
            UserController.CreateUserRequest request = new UserController.CreateUserRequest();
            request.setUser(testUserDTO);
            request.setPassword("password123");

            when(userService.createUser(any(UserDTO.class), any()))
                    .thenThrow(new DuplicateResourceException("Utente", "username", "testuser"));

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @WithMockUser
        @DisplayName("should return 400 when validation fails")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Given - UserDTO con campi mancanti
            UserDTO invalidDTO = UserDTO.builder().build();
            UserController.CreateUserRequest request = new UserController.CreateUserRequest();
            request.setUser(invalidDTO);
            request.setPassword("password123");

            // When/Then
            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @WithMockUser
        @DisplayName("should update user successfully")
        void shouldUpdateUserSuccessfully() throws Exception {
            // Given
            UserDTO updateDTO = UserDTO.builder()
                    .username("updateduser")
                    .email("updated@example.com")
                    .firstName("Updated")
                    .lastName("User")
                    .build();

            when(userService.updateUser(eq(1L), any(UserDTO.class))).thenReturn(updateDTO);

            // When/Then
            mockMvc.perform(put("/api/users/1")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Utente aggiornato con successo"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}/password")
    class ChangePassword {

        @Test
        @WithMockUser
        @DisplayName("should change password successfully")
        void shouldChangePasswordSuccessfully() throws Exception {
            // Given
            UserController.ChangePasswordRequest request = new UserController.ChangePasswordRequest();
            request.setOldPassword("oldPassword");
            request.setNewPassword("newPassword123");

            doNothing().when(userService).changePassword(1L, "oldPassword", "newPassword123");

            // When/Then
            mockMvc.perform(patch("/api/users/1/password")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Password cambiata con successo"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/users/{id}/enable and /disable")
    class EnableDisableUser {

        @Test
        @WithMockUser
        @DisplayName("should enable user successfully")
        void shouldEnableUserSuccessfully() throws Exception {
            // Given
            doNothing().when(userService).enableUser(1L);

            // When/Then
            mockMvc.perform(patch("/api/users/1/enable")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Utente abilitato"));
        }

        @Test
        @WithMockUser
        @DisplayName("should disable user successfully")
        void shouldDisableUserSuccessfully() throws Exception {
            // Given
            doNothing().when(userService).disableUser(1L);

            // When/Then
            mockMvc.perform(patch("/api/users/1/disable")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Utente disabilitato"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @WithMockUser
        @DisplayName("should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            // Given
            doNothing().when(userService).deleteUser(1L);

            // When/Then
            mockMvc.perform(delete("/api/users/1")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("Utente eliminato"));
        }

        @Test
        @WithMockUser
        @DisplayName("should return 404 when user not found for deletion")
        void shouldReturn404WhenUserNotFoundForDeletion() throws Exception {
            // Given
            doThrow(new ResourceNotFoundException("Utente", 999L))
                    .when(userService).deleteUser(999L);

            // When/Then
            mockMvc.perform(delete("/api/users/999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }
}