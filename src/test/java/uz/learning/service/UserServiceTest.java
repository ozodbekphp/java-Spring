package uz.learning.service;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import uz.learning.Entity.User;
import uz.learning.Repository.UserRepository;
import uz.learning.Service.UserService;
import uz.learning.dto.request.UserRequest;
import uz.learning.dto.response.UserResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest validRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        validRequest = UserRequest.builder()
                .username("ozodbek")
                .password("password123")
                .build();

        savedUser = User.builder()
                .id(1L)
                .username("ozodbek")
                .password("encoded_password")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tishi kerak")
    void register_Success() {
        // GIVEN: Bazada bu username yo'q deb hisoblaymiz
        when(userRepository.existsByUsername("ozodbek")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // WHEN: Registratsiya funksiyasini chaqiramiz
        UserResponse response = userService.register(validRequest);

        // THEN: Natijani tekshiramiz
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getUsername()).isEqualTo("ozodbek");
        assertThat(response.getMessage()).isEqualTo("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi");

        verify(userRepository).existsByUsername("ozodbek");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Username allaqachon mavjud bo'lsa RuntimeException tashlashi kerak")
    void register_UsernameAlreadyExists_ThrowsException() {
        // GIVEN: Username allaqachon mavjud
        when(userRepository.existsByUsername("ozodbek")).thenReturn(true);

        // WHEN & THEN: Xatolik chiqishini tekshiramiz
        assertThatThrownBy(() -> userService.register(validRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Bu username allaqachon mavjud: ozodbek");

        // Bazaga save chaqirilmasligini tekshiramiz
        verify(userRepository, never()).save(any(User.class));
    }
}
