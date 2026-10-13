package uz.learning.filter;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import uz.learning.Entity.User;
import uz.learning.Repository.UserRepository;

@Component
public class AuthFilter extends OncePerRequestFilter {

    @Autowired(required = false)
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // 1. Ochiq yo'llar: /api/auth/ (login va register) uchun token tekshirilmaydi
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Headerdan "Authorization" qismini o'qiymiz
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || (!authHeader.startsWith("Bearer: ") && !authHeader.startsWith("Bearer "))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter()
                    .write("{\"status\": 401, \"error\" : \"Avtorizatsiyadan o'tilmagan! Token topilmadi.\"}");
            return;
        }

        // "Bearer: " (8 ta belgi) yoki "Bearer " (7 ta belgi) ni kesib olamiz
        String token = authHeader.startsWith("Bearer: ")
                ? authHeader.substring(8).trim()
                : authHeader.substring(7).trim();

        if (token.isBlank()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Token yaroqsiz!\"}");
            return;
        }

        // 3. Token orqali login qilgan user ID sini aniqlaymiz
        Long userId = AuthContext.getUserIdByToken(token);

        if (userId == null && userRepository != null) {
            Optional<User> userOpt = userRepository.findByToken(token);
            if (userOpt.isPresent()) {
                userId = userOpt.get().getId();
                AuthContext.saveToken(token, userId);
            }
        }

        if (userId == null && userRepository != null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\": 401, \"error\": \"Token yaroqsiz yoki foydalanuvchi topilmadi!\"}");
            return;
        }

        // 4. Topilgan userId ni Request va AuthContext ichiga saqlaymiz
        try {
            if (userId != null) {
                request.setAttribute("userId", userId);
                AuthContext.setCurrentUserId(userId);
            }
            filterChain.doFilter(request, response);
        } finally {
            // So'rov yakunlangach xotirani tozalaymiz
            AuthContext.clear();
        }
    }
}
