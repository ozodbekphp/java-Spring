// package uz.learning.filter;

// import java.io.IOException;

// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;

// @Component
// public class AuthFilter extends OncePerRequestFilter {
//     @Override
//     protected void doFilterInternal(HttpServletRequest request,
//             HttpServletResponse response,
//             FilterChain filterChain) throws ServletException, IOException {
//         String path = request.getRequestURI();

//         // 1. Ochiq yo'llar api/auth/ login va register uchun token tekshirilmaydi
//         if (path.startsWith("/api/auth/")) {
//             filterChain.doFilter(request, response);
//             return;
//         }

//         // headerdan "Authorization" qism ni o'qiymiz
//         String authHeader = request.getHeader("Authorization");

//         if (authHeader == null || !authHeader.startsWith("Bearer: ")) {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.setContentType("application/json");
//             response.setCharacterEncoding("UTF-8");
//             response.getWriter()
//                     .write("{\"status\": 401, \"error\" : \"Avtorizatsiyadan o'tilmagan! Token topilmadi.\"}");
//             return;
//         }

//         String token = authHeader.substring(7);

//         if (token.isBlank()) {
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.getWriter().write("{\"status\": 401, \"error\": \"Token yaroqsiz!\"}");
//             return;
//         }

//         filterChain.doFilter(request, response);
//     }
// }
