package com.university.portal.service;

import com.university.portal.config.JwtTokenProvider;
import com.university.portal.entity.Role;
import com.university.portal.entity.User;
import com.university.portal.exception.ResourceNotFoundException;
import com.university.portal.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void changePasswordStoresEncodedNewPassword() {
        User user = new User("student", "old-hash", "student@university.edu", Role.ROLE_STUDENT);
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

        authService.changePassword("student", "old-password", "new-password", "new-password");

        assertEquals("new-hash", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() {
        User user = new User("student", "old-hash", "student@university.edu", Role.ROLE_STUDENT);
        when(userRepository.findByUsername("student")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("student", "wrong-password", "new-password", "new-password"));

        assertEquals("old-hash", user.getPassword());
        verify(userRepository, never()).save(user);
    }

    @Test
    void changePasswordRejectsMismatchedConfirmation() {
        assertThrows(IllegalArgumentException.class,
                () -> authService.changePassword("student", "old-password", "new-password", "different-password"));

        verify(userRepository, never()).findByUsername("student");
    }
}
