package it.aruba.pec.zeta.user.unit.mapper;

import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("UserMapper Unit Tests")
class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    @DisplayName("toDTO should map entity to DTO correctly")
    void toDTOShouldMapEntityToDTOCorrectly() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        User user = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // When
        UserDTO dto = userMapper.toDTO(user);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getUsername()).isEqualTo("testuser");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getFirstName()).isEqualTo("Test");
        assertThat(dto.getLastName()).isEqualTo("User");
        assertThat(dto.isEnabled()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("toDTO should return null for null entity")
    void toDTOShouldReturnNullForNullEntity() {
        // When
        UserDTO dto = userMapper.toDTO(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    @DisplayName("toEntity should map DTO to entity correctly")
    void toEntityShouldMapDTOToEntityCorrectly() {
        // Given
        UserDTO dto = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .build();

        // When
        User entity = userMapper.toEntity(dto);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getUsername()).isEqualTo("testuser");
        assertThat(entity.getEmail()).isEqualTo("test@example.com");
        assertThat(entity.getFirstName()).isEqualTo("Test");
        assertThat(entity.getLastName()).isEqualTo("User");
        assertThat(entity.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("toEntity should return null for null DTO")
    void toEntityShouldReturnNullForNullDTO() {
        // When
        User entity = userMapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("updateEntityFromDTO should update entity fields")
    void updateEntityFromDTOShouldUpdateEntityFields() {
        // Given
        User entity = User.builder()
                .id(1L)
                .username("olduser")
                .email("old@example.com")
                .firstName("Old")
                .lastName("Name")
                .enabled(true)
                .build();

        UserDTO dto = UserDTO.builder()
                .username("newuser")
                .email("new@example.com")
                .firstName("New")
                .lastName("Name")
                .enabled(false)
                .build();

        // When
        userMapper.updateEntityFromDTO(dto, entity);

        // Then
        assertThat(entity.getUsername()).isEqualTo("newuser");
        assertThat(entity.getEmail()).isEqualTo("new@example.com");
        assertThat(entity.getFirstName()).isEqualTo("New");
        assertThat(entity.getLastName()).isEqualTo("Name");
        assertThat(entity.isEnabled()).isFalse();
    }

    @Test
    @DisplayName("updateEntityFromDTO should preserve existing values for null DTO fields")
    void updateEntityFromDTOShouldPreserveExistingValuesForNullDTOFields() {
        // Given
        User entity = User.builder()
                .id(1L)
                .username("existinguser")
                .email("existing@example.com")
                .firstName("Existing")
                .lastName("Name")
                .enabled(true)
                .build();

        UserDTO dto = UserDTO.builder()
                .firstName("Updated")  // Solo firstName viene aggiornato
                .build();

        // When
        userMapper.updateEntityFromDTO(dto, entity);

        // Then
        assertThat(entity.getUsername()).isEqualTo("existinguser"); // Preserved
        assertThat(entity.getEmail()).isEqualTo("existing@example.com"); // Preserved
        assertThat(entity.getFirstName()).isEqualTo("Updated"); // Updated
        assertThat(entity.getLastName()).isEqualTo("Name"); // Preserved
    }

    @Test
    @DisplayName("updateEntityFromDTO should handle null inputs gracefully")
    void updateEntityFromDTOShouldHandleNullInputsGracefully() {
        // Given
        User entity = User.builder().username("test").build();

        // When/Then - should not throw exception
        userMapper.updateEntityFromDTO(null, entity);
        userMapper.updateEntityFromDTO(UserDTO.builder().build(), null);
    }
}