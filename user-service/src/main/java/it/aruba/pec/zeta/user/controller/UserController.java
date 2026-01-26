package it.aruba.pec.zeta.user.controller;

import it.aruba.pec.zeta.common.dto.ApiResponse;
import it.aruba.pec.zeta.common.dto.PagedResponse;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@PathVariable Long id) {
        log.debug("GET /api/users/{}", id);

        UserDTO user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        log.debug("GET /api/users?page={}&size={}&sortBy={}&sortDir={}", page, size, sortBy, sortDir);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        PagedResponse<UserDTO> users = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<UserDTO>>> searchUsers(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        log.debug("GET /api/users/search?q={}&page={}&size={}", q, page, size);

        Pageable pageable = PageRequest.of(page, size);
        PagedResponse<UserDTO> users = userService.searchUsers(q, pageable);

        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByUsername(@PathVariable String username) {
        log.debug("GET /api/users/username/{}", username);

        UserDTO user = userService.getUserByUsername(username);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<UserDTO>> getUserByEmail(@PathVariable String email) {
        log.debug("GET /api/users/email/{}", email);

        UserDTO user = userService.getUserByEmail(email);

        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserDTO>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        log.info("POST /api/users - username: {}", request.getUser().getUsername());

        UserDTO createdUser = userService.createUser(request.getUser(), request.getPassword());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "Utente creato con successo"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {

        log.info("PUT /api/users/{}", id);

        UserDTO updatedUser = userService.updateUser(id, userDTO);

        return ResponseEntity.ok(ApiResponse.success(updatedUser, "Utente aggiornato con successo"));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestBody ChangePasswordRequest request) {

        log.info("PATCH /api/users/{}/password", id);

        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.success(null, "Password cambiata con successo"));
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<ApiResponse<Void>> enableUser(@PathVariable Long id) {
        log.info("PATCH /api/users/{}/enable", id);

        userService.enableUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Utente abilitato"));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<ApiResponse<Void>> disableUser(@PathVariable Long id) {
        log.info("PATCH /api/users/{}/disable", id);

        userService.disableUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Utente disabilitato"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        log.info("DELETE /api/users/{}", id);

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Utente eliminato"));
    }

    // Classi per i body delle richieste
    @lombok.Data
    public static class CreateUserRequest {
        @Valid
        private UserDTO user;
        private String password;
    }

    @lombok.Data
    public static class ChangePasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}