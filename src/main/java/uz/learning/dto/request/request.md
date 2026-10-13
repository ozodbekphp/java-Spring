# Java Spring Boot — Request DTO, Validation va Builder metodlari qo'llanmasi

---

## 1. Request DTO nima va unda nimalarni qabul qilish mumkin?

`dto/request` ichidagi klasslar Frontend, Mobil ilova yoki Postman'dan kelayotgan ma'lumotlarni qabul qilish va ularni **tekshirish (Validation)** uchun ishlatiladi *(Laravel'dagi `FormRequest` kabi)*.

Request DTO ichida quyidagi barcha ma'lumot turlarini qabul qilish mumkin:
* **Matnlar:** `String`
* **Butun sonlar:** `Integer`, `Long` (ID lar, narxlar, sonlar uchun)
* **Kasr va pul sonlar:** `Double`, `BigDecimal`
* **Mantiqiy qiymat:** `Boolean` (`true` / `false`)
* **Sana va vaqt:** `LocalDate` (`2026-10-13`), `LocalDateTime` (`2026-10-13T14:30:00`)
* **Ro'yxatlar (Massivlar):** `List<Long>`, `List<String>`, `Set<Long>`
* **Ichma-ich (Nested) DTO obyektlar:** `AddressRequest address` yoki `List<ItemRequest> items`
* **Fayl va rasmlar:** `MultipartFile file` (`multipart/form-data` orqali)

---

## 2. ENG MUHIM FARQ: `@NotNull` vs `@NotEmpty` vs `@NotBlank`

Ko'pchilik dasturchilar shu 3 tasini adashtiradi. Ularning aniq farqi:

| Annotatsiya | Qaysi tipga ishlatiladi? | `null` bo'lsa | `""` (bo'sh) bo'lsa | `"   "` (faqat probel) bo'lsa |
|---|---|---|---|---|
| **`@NotNull`** | **Sonlar (`Long`, `Integer`), `Boolean`, `LocalDate`, Obyektlar** | ❌ Xato | ✅ O'tadi | ✅ O'tadi |
| **`@NotEmpty`** | **Ro'yxatlar (`List`, `Set`, `Map`) va `String`** | ❌ Xato | ❌ Xato | ✅ O'tadi |
| **`@NotBlank`** | **Faqat `String` (Matnlar) uchun!** | ❌ Xato | ❌ Xato | ❌ Xato |

> **Oltin qoida:**
> * `String` uchun har doim -> **`@NotBlank`** ishlating!
> * `Long`, `Integer`, `Boolean`, `LocalDate` uchun har doim -> **`@NotNull`** ishlating (`@NotBlank` sonlarga ishlamaydi, xato beradi!).
> * `List<...>` uchun har doim -> **`@NotEmpty`** ishlating!

---

## 3. Request ichida ishlatiladigan barcha Validation annotatsiyalari

Ushbu annotatsiyalar `import jakarta.validation.constraints.*;` paketidan olinadi.
*(Ishlashi uchun Controllerda `@Valid @RequestBody` yozilishi shart!)*

### A) Matnlar (`String`) uchun:
```java
// 1. Bo'sh bo'lmasligi va faqat probeldan iborat bo'lmasligi shart
@NotBlank(message = "Ism kiritish majburiy")
private String name;

// 2. Uzunlik chegarasi (min va max)
@Size(min = 3, max = 50, message = "Username 3 tadan 50 tagacha belgidan iborat bo'lishi kerak")
private String username;

// 3. Email formatini tekshirish (@ belgisi va domen borligi)
@Email(message = "Email formati noto'g'ri kiritildi")
private String email;

// 4. Regex (Regular Expression) orqali tekshirish (Masalan: O'zbekiston telefon raqami)
@Pattern(regexp = "^\\+998[0-9]{9}$", message = "Telefon raqam +998901234567 formatida bo'lishi kerak")
private String phoneNumber;
```

### B) Sonlar (`Long`, `Integer`, `BigDecimal`, `Double`) uchun:
```java
// 1. Qiymat berilishi majburiy (null bo'lmasin)
@NotNull(message = "Narx kiritish majburiy")
private Long price;

// 2. Musbat son (> 0, ya'ni 0 dan katta bo'lishi shart)
@Positive(message = "Narx 0 dan katta bo'lishi kerak")
private Long price;

// 3. 0 yoki musbat son (>= 0, masalan ombordagi qoldiq 0 bo'lishi mumkin)
@PositiveOrZero(message = "Mahsulot soni manfiy bo'lishi mumkin emas")
private Integer count;

// 4. Eng kichik va eng katta qiymat chegarasi (@Min va @Max)
@Min(value = 18, message = "Yosh kamida 18 da bo'lishi kerak")
@Max(value = 100, message = "Yosh 100 dan oshmasligi kerak")
private Integer age;

// 5. Kasr sonlar uchun chegara (@DecimalMin, @DecimalMax)
@DecimalMin(value = "0.01", message = "Foiz kamida 0.01 bo'lishi kerak")
@DecimalMax(value = "100.00", message = "Foiz 100 dan oshmasligi kerak")
private BigDecimal percent;

// 6. Xonalar sonini cheklash (integer = butun qism, fraction = kasr qism)
@Digits(integer = 10, fraction = 2, message = "Summa formati noto'g'ri")
private BigDecimal amount;
```

### C) Ro'yxatlar (`List`, `Set`) va Ichma-ich obyektlar uchun:
```java
// 1. Ro'yxat null ham bo'lmasin, ichida kamida 1 ta element bo'lsin
@NotEmpty(message = "Kamida bitta mahsulot tanlanishi shart")
private List<Long> productIds;

// 2. Ro'yxat ichidagi elementlar sonini cheklash
@Size(min = 1, max = 10, message = "1 tadan 10 tagacha rasm yuklash mumkin")
private List<String> images;

// 3. Ichma-ich (Nested) DTO ni ham tekshirish uchun ustiga @Valid qo'yiladi
@Valid
@NotNull(message = "Manzil ma'lumotlari kiritilishi shart")
private AddressRequest address;
```

### D) Sanalar (`LocalDate`, `LocalDateTime`) uchun:
```java
// 1. O'tmishdagi sana bo'lishi shart (Masalan: tug'ilgan sana)
@Past(message = "Tug'ilgan sana o'tmishda bo'lishi kerak")
private LocalDate birthDate;

// 2. O'tmish yoki bugungi sana
@PastOrPresent(message = "Sana kelajakda bo'lishi mumkin emas")
private LocalDate startDate;

// 3. Kelajakdagi sana bo'lishi shart (Masalan: shartnoma tugash muddati)
@Future(message = "Muddat kelajakdagi sana bo'lishi kerak")
private LocalDate expireDate;

// 4. Bugun yoki kelajakdagi sana
@FutureOrPresent(message = "Yetkazib berish sanasi bugundan oldin bo'lishi mumkin emas")
private LocalDateTime deliveryTime;
```

### E) Mantiqiy (`Boolean`) uchun:
```java
// Oferta shartlariga rozilik (faqat true bo'lishi shart)
@AssertTrue(message = "Shartlarga rozilik bildirishingiz shart")
private Boolean agreedToTerms;
```

---

## 4. Lombok annotatsiyalari va Request'dagi tayyor metodlar (`build`, `get`, `set`)

Request klass tepasiga quyidagi Lombok annotatsiyalarini qo'yganingizda, Java siz uchun ko'plab tayyor metodlarni avtomatik yaratib beradi:

```java
@Getter               // Barcha fieldlar uchun get...() metodlarini yaratadi
@Setter               // Barcha fieldlar uchun set...() metodlarini yaratadi
@NoArgsConstructor    // Bo'sh konstruktor: new ProductRequest() (Jackson JSON o'qishi uchun SHART!)
@AllArgsConstructor   // Barcha parametrli konstruktor: new ProductRequest("iPhone", 1200L, 5)
@Builder              // Zanjir usulida obyekt qurish: ProductRequest.builder()...build()
public class ProductRequest {
    private String name;
    private Long price;

    @Builder.Default  // Agar qiymat berilmasa, standart (default) qiymat oladi
    private Integer count = 1;
}
```

### Ushbu klassda avtomatik paydo bo'ladigan metodlar va ularni ishlatish:

```java
// 1. Builder orqali obyekt yasash (.builder() va .build())
ProductRequest req = ProductRequest.builder()
        .name("MacBook Pro")
        .price(2000L)
        .count(5)
        .build();

// 2. Getter metodlari — Request ichidagi qiymatlarni o'qib olish
String nomi = req.getName();      // "MacBook Pro"
Long narxi = req.getPrice();      // 2000L
Integer soni = req.getCount();    // 5

// 3. Setter metodlari — Request ichidagi qiymatni o'zgartirish
req.setName("MacBook Air M3");
req.setPrice(1500L);
req.setCount(10);

// 4. Matn (String) field ustida eng ko'p ishlatiladigan foydali metodlar:
String cleanName = req.getName().trim();           // Bosh va oxiridagi probellarni olib tashlash
String lowerName = req.getName().toLowerCase();    // Kichik harflarga o'tkazish
String upperName = req.getName().toUpperCase();    // Katta harflarga o'tkazish
boolean isBlank = req.getName().isBlank();         // Bo'shligini tekshirish
```

---

## 5. JSON (Jackson) annotatsiyalari — Frontend bilan moslashish

Ba'zida Frontend `product_id` (snake_case) yuboradi, Java'da esa `productId` (camelCase) yoziladi. Shunda quyidagi annotatsiyalar yordam beradi:

```java
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;

// JSON ichida bizda yo'q ortiqcha maydonlar kelsa xato bermasdan o'tkazib yuboradi
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    // Frontend "product_id" deb yuborsa ham, Java'dagi productId ga tushadi
    @JsonProperty("product_id")
    @NotNull(message = "Mahsulot ID si majburiy")
    private Long productId;

    // Sanani maxsus formatda qabul qilish (masalan: "13.10.2026")
    @JsonFormat(pattern = "dd.MM.yyyy")
    private LocalDate orderDate;
}
```

---

## 6. Controllerda Request'ni qabul qilishning 4 xil usuli

```java
// 1. @RequestBody — JSON body orqali kelganda (POST, PUT)
// @Valid qo'yilmasa, @NotBlank va @NotNull tekshiruvlari ISHLAMAYDI!
@PostMapping
public ResponseEntity<ProductResponse> store(@Valid @RequestBody ProductRequest request) { ... }

// 2. @PathVariable — URL ichidagi o'zgaruvchini olish (/api/products/15)
@GetMapping("/{id}")
public ResponseEntity<ProductResponse> show(@PathVariable Long id) { ... }

// 3. @RequestParam — URL so'rovidan olish (/api/products?search=iphone&page=0)
@GetMapping
public ResponseEntity<?> index(
        @RequestParam(required = false) String search,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
) { ... }

// 4. @RequestAttribute — Filter (masalan AuthFilter) biriktirgan ma'lumotni olish
@PostMapping("/my-product")
public ResponseEntity<?> createMyProduct(
        @Valid @RequestBody ProductRequest request,
        @RequestAttribute("userId") Long userId
) { ... }
```
