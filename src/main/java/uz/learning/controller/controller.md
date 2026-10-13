# Java Spring Boot — Controller yozish va Response qaytarish bo'yicha to'liq qo'llanma

---

## 1. Controller nima va uning asosiy annotatsiyalari

Controller — foydalanuvchidan (Frontend, Mobil ilova, Postman) kelgan HTTP so'rovni qabul qilib, uni `Service` ga uzatadigan va qaytgan natijani HTTP javob (`ResponseEntity`) sifatida qaytaradigan qatlamdir.

```java
@RestController                     // Klassning barcha metodlari avtomatik JSON qaytarishini bildiradi
@RequestMapping("/api/products")    // Shu controllerdagi barcha URLlar "/api/products" bilan boshlanadi
@RequiredArgsConstructor            // "private final" bo'lgan Service larni avtomatik constructor orqali ulaydi
public class ProductController {

    private final ProductInterface productService; // Service qatlami
}
```

---

## 2. HTTP Metodlar — Qaysi amal uchun qaysi annotatsiya ishlatiladi?

| Annotatsiya | HTTP Metod | Vazifasi | URL misoli |
|---|---|---|---|
| **`@GetMapping`** | `GET` | Ma'lumotlarni o'qish (ro'yxat yoki bitta element) | `GET /api/products` yoki `GET /api/products/5` |
| **`@PostMapping`** | `POST` | Yangi ma'lumot yaratish (store, login, register) | `POST /api/products` |
| **`@PutMapping`** | `PUT` | Mavjud ma'lumotni to'liq yangilash | `PUT /api/products/5` |
| **`@PatchMapping`** | `PATCH` | Ma'lumotning faqat 1 ta ustunini (masalan status/active) o'zgartirish | `PATCH /api/products/5/active` |
| **`@DeleteMapping`** | `DELETE` | Ma'lumotni o'chirish | `DELETE /api/products/5` |

---

## 3. Controllerda so'rov ma'lumotlarini qabul qilishning 6 xil usuli

```java
// 1. @Valid @RequestBody — JSON body qabul qilish va validatsiyadan o'tkazish (POST, PUT)
@PostMapping
public ResponseEntity<ProductResponse> store(@Valid @RequestBody ProductRequest request) { ... }

// 2. @PathVariable — URL yo'lidagi o'zgaruvchini olish (masalan: /api/products/12)
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> show(@PathVariable Long id) { ... }

// 3. @RequestParam — URL so'rov parametrlarini olish (masalan: /api/products?search=iphone&page=0&size=10)
@GetMapping
public ResponseEntity<ProductPaginationResponse> index(
        @RequestParam(required = false) String search,       // Majburiy emas (kelmasa null bo'ladi)
        @RequestParam(defaultValue = "0") int page,          // Kelmasa standart 0 oladi
        @RequestParam(defaultValue = "10") int size          // Kelmasa standart 10 oladi
) { ... }

// 4. @RequestAttribute — Filter (masalan AuthFilter) biriktirgan ma'lumotni (userId) olish
@GetMapping("/my")
public ResponseEntity<?> getMyProducts(@RequestAttribute("userId") Long userId) { ... }

// 5. @RequestHeader — HTTP Header ichidan ma'lumot o'qish
@GetMapping("/info")
public ResponseEntity<?> getInfo(
        @RequestHeader("Authorization") String token,
        @RequestHeader(value = "Accept-Language", defaultValue = "uz") String lang
) { ... }

// 6. @RequestParam("file") MultipartFile — Rasm yoki fayl yuklash (multipart/form-data)
@PostMapping("/upload")
public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) { ... }
```

---

## 4. Qaysi holatda qanday HTTP Status va Response qaytariladi?

REST API standartiga ko'ra har bir holatning o'z status kodi bor:

| Status Kod | Spring Enum nomi | Qaysi holatda ishlatiladi? | Qanday yoziladi? |
|---|---|---|---|
| **200 OK** | `HttpStatus.OK` | Ma'lumot muvaffaqiyatli olindi (`GET`), yangilandi (`PUT`/`PATCH`) yoki o'chirildi (`DELETE` xabar bilan) | `return ResponseEntity.ok(response);` |
| **201 CREATED** | `HttpStatus.CREATED` | Yangi ma'lumot muvaffaqiyatli bazaga qo'shildi (`POST`: register, store) | `return ResponseEntity.status(HttpStatus.CREATED).body(response);` |
| **202 ACCEPTED** | `HttpStatus.ACCEPTED` | So'rov qabul qilindi va fonda (RabbitMQ / Queue da) bajarilmoqda | `return ResponseEntity.accepted().body(response);` |
| **204 NO CONTENT** | `HttpStatus.NO_CONTENT` | Ma'lumot o'chirildi, lekin javobda hech qanday JSON qaytarish shart emas | `return ResponseEntity.noContent().build();` |
| **400 BAD REQUEST** | `HttpStatus.BAD_REQUEST` | Foydalanuvchi noto'g'ri ma'lumot yubordi yoki validatsiya xatosi | `return ResponseEntity.badRequest().body(error);` |
| **401 UNAUTHORIZED** | `HttpStatus.UNAUTHORIZED` | Foydalanuvchi login qilmagan yoki Token yubormagan / yaroqsiz | `return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);` |
| **403 FORBIDDEN** | `HttpStatus.FORBIDDEN` | Login qilgan, lekin bu amalga ruxsati yo'q (masalan birovning mahsulotini o'chirish) | `return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);` |
| **404 NOT FOUND** | `HttpStatus.NOT_FOUND` | Bazada so'ralgan ID bo'yicha ma'lumot topilmadi | `return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);` |
| **409 CONFLICT** | `HttpStatus.CONFLICT` | Dublikat xatosi (masalan: bu username yoki telefon raqam allaqachon mavjud) | `return ResponseEntity.status(HttpStatus.CONFLICT).body(error);` |
| **500 SERVER ERROR** | `HttpStatus.INTERNAL_SERVER_ERROR` | Server yoki baza bilan bog'liq kutilmagan texnik xatolik | `return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);` |

---

## 5. Turli xil ko'rinishda Response qaytarish namunalari

### 1) Bitta obyekt qaytarish (`200 OK` va `201 CREATED`)
```java
// Yangi yaratilganda — 201 CREATED
@PostMapping
public ResponseEntity<ProductResponse> store(@Valid @RequestBody ProductRequest request) {
    ProductResponse response = productService.store(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

// Yangilanganda — 200 OK
@PutMapping("/{id}")
public ResponseEntity<ProductResponse> update(
        @PathVariable Long id,
        @Valid @RequestBody ProductRequest request
) {
    ProductResponse response = productService.update(id, request);
    return ResponseEntity.ok(response);
}
```

### 2) Ro'yxat (`List`) qaytarish
```java
@GetMapping("/{productId}")
public ResponseEntity<List<MessageResponse>> index(@PathVariable Long productId) {
    List<MessageResponse> list = messageService.index(productId);
    return ResponseEntity.ok(list);
}
```

### 3) Sahifalangan (`Pagination`) javob qaytarish
```java
@GetMapping
public ResponseEntity<ProductPaginationResponse> index(
        @RequestParam(required = false) String search
) {
    ProductPaginationResponse response = productService.index(search);
    return ResponseEntity.ok(response);
}
```

### 4) DTO yaratmasdan tezkor `Map` (kalit-qiymat JSON) qaytarish
Agar faqatgina `{"message": "Holati o'zgartirildi"}` kabi bitta kichik xabar qaytarmoqchi bo'lsangiz:
```java
@PatchMapping("/{id}/toggle")
public ResponseEntity<Map<String, Object>> toggleActive(@PathVariable Long id) {
    productService.toggleActive(id);
    return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Muvaffaqiyatli o'zgartirildi"
    ));
}
```

### 5) Hech qanday body qaytarmaslik (`Void` — `204 NO CONTENT`)
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();
}
```

### 6) Fayl, Rasm, Excel yoki PDF yuklab berish (`Resource`)
```java
@GetMapping("/download/{filename}")
public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
    Resource file = fileService.loadFile(filename);
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(file);
}
```

---

## 6. To'liq namunaviy CRUD Controller (Yodda saqlash uchun shablon)

```java
package uz.learning.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.learning.Service.ProductInterface;
import uz.learning.dto.request.ProductRequest;
import uz.learning.dto.response.ProductPaginationResponse;
import uz.learning.dto.response.ProductResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductInterface productService;

    // 1. YARATISH (POST /api/products) -> 201 CREATED
    @PostMapping
    public ResponseEntity<ProductResponse> store(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.store(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. RO'YXAT OLISH (GET /api/products?search=...) -> 200 OK
    @GetMapping
    public ResponseEntity<ProductPaginationResponse> index(
            @RequestParam(required = false) String search
    ) {
        ProductPaginationResponse response = productService.index(search);
        return ResponseEntity.ok(response);
    }

    // 3. YANGILASH (PUT /api/products/{id}) -> 200 OK
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(response);
    }

    // 4. O'CHIRISH (DELETE /api/products/{id}) -> 200 OK
    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponse> delete(@PathVariable Long id) {
        ProductResponse response = productService.delete(id);
        return ResponseEntity.ok(response);
    }
}
```
