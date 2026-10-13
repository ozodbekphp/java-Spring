# Java Spring Boot — Response DTO, JSON formatlash va Mapping qo'llanmasi

---

## 1. Response DTO nima va nima uchun kerak?

`dto/response` papkasi ichidagi klasslar Backend'dan Frontend yoki Mobil ilovaga **JSON ko'rinishida qaytadigan ma'lumotlar qolipi** hisoblanadi *(Laravel'dagi `JsonResource` / `UniversalResource` kabi)*.

### Nega to'g'ridan-to'g'ri `@Entity` (`User`, `Product`) qaytarilmaydi?
1. **Xavfsizlik:** `User` entity ichida `password` va `token` bor. Agar `User` ni to'g'ridan-to'g'ri qaytarsangiz, maxfiy ma'lumotlar foydalanuvchiga ko'rinib qoladi.
2. **Moslashuvchanlik:** Bazada yo'q, lekin javobda kerak bo'lgan qo'shimcha maydonlarni (masalan: `message`, `totalPage`, `fullName`) qo'shib yuborish mumkin.
3. **Mustaqillik:** Bazadagi ustun nomi o'zgarsa ham, Frontendga boradigan JSON formati buzilmaydi.

---

## 2. Loyihalarda eng ko'p ishlatiladigan 5 xil Response DTO turi

### 1) Oddiy Ma'lumot Response (`ProductResponse`, `UserResponse`)
Bitta obyekt ma'lumotlarini qaytarish uchun:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private Integer count;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
```

### 2) Sahifalash (Pagination) Response (`ProductPaginationResponse`)
Ro'yxat va sahifa ma'lumotlarini birga qaytarish uchun:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPaginationResponse {
    private List<ProductResponse> products; // Asosiy ro'yxat
    private int currentPage;                // Hozirgi sahifa (0 dan boshlanadi)
    private int totalPage;                  // Jami sahifalar soni
    private long totalElements;             // Bazadagi jami elementlar soni
}
```

### 3) Avtorizatsiya Response (`AuthResponse`)
Login bo'lganda token va foydalanuvchi ma'lumotini qaytarish uchun:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String message;
}
```

### 4) Universal (Umumiy) `ApiResponse<T>` qolipi
Katta loyihalarda barcha javoblar bir xil standartda (`success`, `message`, `data`) qaytishi uchun Generic `<T>` klass ishlatiladi:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data; // Bu yerga ProductResponse, List<UserResponse> va h.k. tushaveradi
}
```

### 5) Xatolik Response (`ErrorResponse`)
Xatolik yuz berganda (`GlobalExceptionHandler` ichida) qaytarish uchun:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {
    private int status;                 // Masalan: 400, 401, 404
    private String message;             // Asosiy xato xabari
    private Map<String, String> errors; // Validation xatolari ro'yxati
    private LocalDateTime timestamp;    // Xato vaqti
}
```

---

## 3. Response DTO ichida ishlatiladigan muhim JSON (`Jackson`) annotatsiyalari

Ushbu annotatsiyalar `com.fasterxml.jackson.annotation.*` paketidan olinadi va JSON ko'rinishini boshqaradi:

```java
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;

// 1. ENG FOYDALI ANNOTATSIYA: Qiymati null bo'lgan maydonlarni JSON da umuman chiqarmaydi!
// Masalan: message = null bo'lsa, JSON da "message": null bo'lib ko'rinmaydi.
@JsonInclude(JsonInclude.Include.NON_NULL)

// 2. JSON da kalitlar qaysi tartibda chiqishini belgilash
@JsonPropertyOrder({"id", "message", "createdAt", "updatedAt"})
public class MessageResponse {

    private Long id;
    private String message;

    // 3. JSON da ustun nomini o'zgartirib chiqarish (camelCase -> snake_case)
    // Java'da createdAt, lekin Postman/Frontendga "created_at" bo'lib boradi
    @JsonProperty("created_at")
    // 4. Sanani chiroyli va o'qishli formatda chiqarish ("2026-10-13 10:30:00")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 5. Ushbu maydonni JSON javobda butunlay yashirish
    @JsonIgnore
    private String internalSecretNote;
}
```

---

## 4. Entity'dan Response DTO'ga o'girishning (Mapping) 3 xil usuli

### 1-usul: Service ichida `.builder()` orqali yasash (Bitta obyekt uchun)
```java
Product savedProduct = productRepository.save(product);

return ProductResponse.builder()
        .id(savedProduct.getId())
        .name(savedProduct.getName())
        .price(savedProduct.getPrice())
        .count(savedProduct.getCount())
        .createdAt(savedProduct.getCreatedAt())
        .updatedAt(savedProduct.getUpdatedAt())
        .message("Mahsulot muvaffaqiyatli saqlandi")
        .build();
```

### 2-usul: `stream().map(...)` orqali ro'yxatni o'girish (`List<Entity>` -> `List<Response>`)
```java
List<Message> messages = messageRepository.findAllByProductId(productId);

List<MessageResponse> responseList = messages.stream()
        .map(m -> MessageResponse.builder()
                .id(m.getId())
                .message(m.getMessage())
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .build())
        .toList();
```

### 3-usul (Professional usul): Response klassning o'zida `fromEntity(...)` metodini yozish
Har safar Service ichida `.builder()...` ni qayta-qayta yozmaslik uchun `ProductResponse` klassining ichiga bitta tayyor static metod yozib qo'yiladi:

```java
// ProductResponse.java ichida:
public static ProductResponse fromEntity(Product p, String msg) {
    return ProductResponse.builder()
            .id(p.getId())
            .name(p.getName())
            .price(p.getPrice())
            .count(p.getCount())
            .createdAt(p.getCreatedAt())
            .updatedAt(p.getUpdatedAt())
            .message(msg)
            .build();
}

// Service ichida esa 1 qator kod bilan chaqiriladi:
return ProductResponse.fromEntity(savedProduct, "Mahsulot muvaffaqiyatli saqlandi");
```

---

## 5. Controllerda Response DTO ni qaytarish (`ResponseEntity`)

```java
// 1. 200 OK bilan qaytarish (GET, PUT, DELETE)
return ResponseEntity.ok(response);

// 2. 201 CREATED bilan qaytarish (POST - yangi yaratilganda)
return ResponseEntity.status(HttpStatus.CREATED).body(response);

// 3. Ro'yxat (List) qaytarish
public ResponseEntity<List<MessageResponse>> index(@PathVariable Long id) {
    return ResponseEntity.ok(messageService.index(id));
}
```
