package uz.learning.Service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uz.learning.Entity.User;
import uz.learning.Repository.UserRepository;
import uz.learning.dto.request.UserRequest;
import uz.learning.dto.response.AuthResponse;
import uz.learning.dto.response.UserResponse;
import uz.learning.filter.AuthContext;

@Service
@RequiredArgsConstructor
public class UserService implements UserInterface {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public UserResponse register(UserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Bu username allaqachon mavjud: " + request.getUsername());
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .message("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi")
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    @Override
    public AuthResponse login(UserRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Username yoki parol noto'g'ri!"));

        // kiritilgan ochiq parol ni bazadan hash parol bilan tekshirish decode

        boolean isPasswordMatch = passwordEncoder.matches(request.getPassword(), user.getPassword());
        if (!isPasswordMatch) {
            throw new RuntimeException("Username yoki parol noto'g'ri!");
        }

        String token = UUID.randomUUID().toString();

        // Tokenni bazaga (users jadvaliga) va xotiraga saqlaymiz
        user.setToken(token);
        userRepository.save(user);
        AuthContext.saveToken(token, user.getId());

        return AuthResponse.builder()
                .token(token)
                .build();
    }
}
