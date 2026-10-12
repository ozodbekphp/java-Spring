package uz.learning.controller;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import uz.learning.Service.UserInterface;
import uz.learning.dto.request.UserRequest;
import uz.learning.dto.response.UserResponse;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserInterface userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("To'g'ri ma'lumotlar bilan 201 CREATED qaytishi kerak")
    void register_ValidRequest_Returns201() throws Exception {
        // GIVEN
        UserRequest request = UserRequest.builder()
                .username("ozodbek")
                .password("password123")
                .build();

        UserResponse mockResponse = UserResponse.builder()
                .id(1L)
                .username("ozodbek")
                .message("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi")
                .createdAt(LocalDateTime.now())
                .build();

        when(userService.register(any(UserRequest.class))).thenReturn(mockResponse);

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.username").value("ozodbek"))
                .andExpect(jsonPath("$.message").value("Foydalanuvchi muvaffaqiyatli ro'yxatdan o'tdi"));
    }

    @Test
    @DisplayName("Bo'sh username yoki qisqa parol berilsa 400 Bad Request qaytishi kerak")
    void register_InvalidRequest_Returns400() throws Exception {
        // GIVEN: Noto'g'ri (bo'sh username va qisqa parol) so'rov
        UserRequest invalidRequest = UserRequest.builder()
                .username("")
                .password("12") // 3 tadan kam
                .build();

        // WHEN & THEN
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
