package it.aruba.pec.zeta.user.service;

import it.aruba.pec.zeta.common.dto.PagedResponse;
import it.aruba.pec.zeta.common.dto.UserDTO;
import it.aruba.pec.zeta.common.exception.DuplicateResourceException;
import it.aruba.pec.zeta.common.exception.ResourceNotFoundException;
import it.aruba.pec.zeta.common.exception.ValidationException;
import it.aruba.pec.zeta.user.entity.User;
import it.aruba.pec.zeta.user.mapper.UserMapper;
import it.aruba.pec.zeta.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserDTO getUserById(Long id) {
        log.debug("Recupero utente con id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        log.debug("Recupero utente con username: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", username));

        return userMapper.toDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        log.debug("Recupero utente con email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("email", email));

        return userMapper.toDTO(user);
    }

    public PagedResponse<UserDTO> getAllUsers(Pageable pageable) {
        log.debug("Recupero utenti paginati: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        Page<User> userPage = userRepository.findAll(pageable);

        return toPagedResponse(userPage);
    }

    public PagedResponse<UserDTO> getActiveUsers(Pageable pageable) {
        log.debug("Recupero utenti attivi paginati");

        Page<User> userPage = userRepository.findByEnabledTrue(pageable);

        return toPagedResponse(userPage);
    }

    public PagedResponse<UserDTO> searchUsers(String search, Pageable pageable) {
        log.debug("Ricerca utenti con query: {}", search);

        Page<User> userPage = userRepository.searchUsers(search, pageable);

        return toPagedResponse(userPage);
    }

    @Transactional
    public UserDTO createUser(UserDTO dto, String rawPassword) {
        log.info("Creazione nuovo utente: {}", dto.getUsername());

        validateNewUser(dto);

        User user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setRole(User.UserRole.USER);

        User savedUser = userRepository.save(user);
        log.info("Utente creato con id: {}", savedUser.getId());

        return userMapper.toDTO(savedUser);
    }

    @Transactional
    public UserDTO updateUser(Long id, UserDTO dto) {
        log.info("Aggiornamento utente con id: {}", id);

        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        validateUserUpdate(dto, existingUser);

        userMapper.updateEntityFromDTO(dto, existingUser);

        User updatedUser = userRepository.save(existingUser);
        log.info("Utente aggiornato: {}", updatedUser.getId());

        return userMapper.toDTO(updatedUser);
    }

    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        log.info("Cambio password per utente id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new ValidationException("Password attuale non corretta");
        }

        if (newPassword.length() < 8) {
            throw new ValidationException("La nuova password deve avere almeno 8 caratteri");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        log.info("Password cambiata per utente id: {}", id);
    }

    @Transactional
    public void enableUser(Long id) {
        log.info("Abilitazione utente id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        user.setEnabled(true);
        userRepository.save(user);

        log.info("Utente abilitato: {}", id);
    }

    @Transactional
    public void disableUser(Long id) {
        log.info("Disabilitazione utente id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utente", id));

        user.setEnabled(false);
        userRepository.save(user);

        log.info("Utente disabilitato: {}", id);
    }

    @Transactional
    public void deleteUser(Long id) {
        log.info("Eliminazione utente id: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Utente", id);
        }

        userRepository.deleteById(id);
        log.info("Utente eliminato: {}", id);
    }

    // === Metodi privati di validazione ===

    private void validateNewUser(UserDTO dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Utente", "username", dto.getUsername());
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Utente", "email", dto.getEmail());
        }
    }

    private void validateUserUpdate(UserDTO dto, User existingUser) {
        if (dto.getUsername() != null &&
                !dto.getUsername().equals(existingUser.getUsername()) &&
                userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Utente", "username", dto.getUsername());
        }

        if (dto.getEmail() != null &&
                !dto.getEmail().equals(existingUser.getEmail()) &&
                userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Utente", "email", dto.getEmail());
        }
    }

    private PagedResponse<UserDTO> toPagedResponse(Page<User> userPage) {
        var userDTOs = userPage.getContent()
                .stream()
                .map(userMapper::toDTO)
                .toList();

        return PagedResponse.of(
                userDTOs,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements()
        );
    }
}