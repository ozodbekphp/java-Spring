package uz.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Foydalanuvchi nomi bo'sh bo'lishi mumkin emas")
    @Size(min = 3, max = 50, message = "Username 2 tadan 50 tagcha belgidan iborat bo'lishi kerak")
    private String username;

    @NotBlank(message = "Parol bo'sh bo'lishi mumkin emas")
    @Size(min = 3, message = "Parol kamida 3 ta belgidan iborat  bo'lishi kerak")
    private String password;
}
