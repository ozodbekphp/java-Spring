package uz.learning.filter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Login qilgan foydalanuvchining ID sini saqlash va istalgan joydan
 * (Controller yoki Service ichidan) olish uchun yordamchi klass.
 * (Laravel'dagi auth()->id() kabi ishlaydi)
 */
public class AuthContext {

    // Har bir HTTP so'rov (thread) uchun alohida userId saqlaydi
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    // Xotirada ham token -> userId bog'lanishini saqlash (tezlik va testlar uchun)
    private static final Map<String, Long> TOKEN_STORE = new ConcurrentHashMap<>();

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    /**
     * Hozirgi login qilgan foydalanuvchi ID sini qaytaradi.
     */
    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }

    public static void saveToken(String token, Long userId) {
        if (token != null && userId != null) {
            TOKEN_STORE.put(token, userId);
        }
    }

    public static Long getUserIdByToken(String token) {
        return TOKEN_STORE.get(token);
    }
}
