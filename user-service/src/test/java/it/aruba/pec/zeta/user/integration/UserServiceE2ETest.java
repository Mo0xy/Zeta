package it.aruba.pec.zeta.user.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.user.config.TestJwtConfig;
import it.aruba.pec.zeta.user.controller.UserController;
import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// Spring Boot 4.x imports
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("User Service End-to-End Tests (H2)")
@Import(TestJwtConfig.class)
class UserServiceE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @Order(1)
    @WithMockUser
    @DisplayName("Full user lifecycle: create -> read -> update -> delete")
    void fullUserLifecycle() throws Exception {
        // Step 1: CREATE
        UserDTO newUser = UserDTO.builder()
                .username("integrationuser")
                .email("integration@example.com")
                .firstName("Integration")
                .lastName("Test")
                .enabled(true)
                .build();

        UserController.CreateUserRequest createRequest = new UserController.CreateUserRequest();
        createRequest.setUser(newUser);
        createRequest.setPassword("password123");

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("integrationuser"));

        // Verifica nel database
        User savedUser = userRepository.findByUsername("integrationuser").orElseThrow();
        assertThat(savedUser.getEmail()).isEqualTo("integration@example.com");
        assertThat(passwordEncoder.matches("password123", savedUser.getPasswordHash())).isTrue();

        Long userId = savedUser.getId();

        // Step 2: READ
        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(userId))
                .andExpect(jsonPath("$.data.username").value("integrationuser"));

        // Step 3: UPDATE
        UserDTO updateDTO = UserDTO.builder()
                .username("integrationuser")
                .email("updated@example.com")
                .firstName("Updated")
                .lastName("Name")
                .enabled(true)
                .build();

        mockMvc.perform(put("/api/users/" + userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utente aggiornato con successo"));

        // Verifica update
        User updatedUser = userRepository.findById(userId).orElseThrow();
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        // Step 4: DELETE
        mockMvc.perform(delete("/api/users/" + userId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Utente eliminato"));

        assertThat(userRepository.findById(userId)).isEmpty();
    }

    @Test
    @Order(2)
    @WithMockUser
    @DisplayName("Should prevent duplicate username")
    void shouldPreventDuplicateUsername() throws Exception {
        // Given
        User existingUser = User.builder()
                .username("duplicatetest")
                .email("first@example.com")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("First")
                .lastName("User")
                .enabled(true)
                .build();
        userRepository.save(existingUser);

        // When
        UserDTO duplicateUser = UserDTO.builder()
                .username("duplicatetest")
                .email("second@example.com")
                .firstName("Second")
                .lastName("User")
                .build();

        UserController.CreateUserRequest request = new UserController.CreateUserRequest();
        request.setUser(duplicateUser);
        request.setPassword("password123");

        // Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }
}