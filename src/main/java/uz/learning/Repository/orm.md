# Java Spring Boot — ORM texnologiyalari va Repository ichida ORM metodlar yasash qo'llanmasi

---

## 1. Java va Spring Boot'dagi mavjud ORM / DB texnologiyalari ro'yxati

| № | Nomi | Tushuntirish | Laravel'dagi o'xshashi |
|---|---|---|---|
| **1** | **Spring Data JPA (Hibernate)** | **Eng mashhuri (85–90% loyihalarda).** Jadvalni Java klassga (`@Entity`) bog'laydi va SQL so'rovlarni avtomatik generatsiya qiladi. | **Eloquent ORM** (`Product::where(...)`) |
| **2** | **jOOQ** | SQL so'rovlarni Java kodi ko'rinishida xatosiz (Type-Safe) yozish vositasi. Murakkab hisobotlar va katta bank/fintech tizimlarida juda mashhur. | **Query Builder** (`DB::table('products')->...`) |
| **3** | **Spring Data JDBC** | Hibernate'ning yengil varianti. Unda murakkab kesh va "lazy-loading" yo'q, oddiy va tez ishlaydi. | Oddiy Model + Query Builder |
| **4** | **MyBatis** | SQL so'rovlarni XML yoki annotatsiyada yozasiz, u esa natijani Java obyektlariga (DTO/Entity) avtomatik bog'lab beradi. | `DB::select(...)` + DTO |
| **5** | **JdbcTemplate** | Eng quyi darajadagi Spring vositasi — toza SQL yozib, ustunlarni qo'lda o'qib olish. | Raw PDO / `DB::statement()` |

---

## 2. `JpaRepository` ichida tayyor keladigan ORM metodlar
`public interface ProductRepository extends JpaRepository<Product, Long>` deb yozganingizda quyidagi metodlar **hech qanday kod yozmasdan** tayyor ishlaydi:

```java
// 1. Yangi ma'lumot qo'shish (INSERT) yoki mavjudini yangilash (UPDATE)
// Agar product.id == null bo'lsa INSERT qiladi, id bor bo'lsa UPDATE qiladi
Product saved = productRepository.save(product);

// 2. Bir nechta ma'lumotni birdaniga saqlash (Batch insert/update)
List<Product> savedList = productRepository.saveAll(productList);

// 3. ID bo'yicha qidirish (Optional qaytaradi)
Optional<Product> productOpt = productRepository.findById(1L);

// 4. ID bo'yicha topish, topilmasa xatolik (Exception) tashlash (Laravel: findOrFail)
Product product = productRepository.findById(1L)
        .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi!"));

// 5. Barcha ma'lumotlarni olish (SELECT * FROM products)
List<Product> allProducts = productRepository.findAll();

// 6. Saralab barchasini olish (ORDER BY id DESC)
List<Product> sorted = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

// 7. Sahifalab olish (Pagination — 0-sahifa, 10 tadan)
Pageable pageable = PageRequest.of(0, 10, Sort.by("id").descending());
Page<Product> page = productRepository.findAll(pageable);

// 8. ID lar ro'yxati bo'yicha olish (WHERE id IN (1, 2, 3))
List<Product> byIds = productRepository.findAllById(List.of(1L, 2L, 3L));

// 9. Shu ID bazada bormi-yo'qmi tekshirish (SELECT EXISTS)
boolean exists = productRepository.existsById(1L);

// 10. Jami qatorlar sonini olish (SELECT COUNT(*))
long totalCount = productRepository.count();

// 11. ID bo'yicha o'chirish (DELETE FROM products WHERE id = ?)
productRepository.deleteById(1L);

// 12. Obyekt bo'yicha o'chirish
productRepository.delete(product);

// 13. Barchasini o'chirish
productRepository.deleteAll();
```

---

## 3. Repository ichida o'zingiz ORM metod yasashning 3 xil usuli

### 1-USUL: Metod nomi orqali avtomatik ORM yasash (Derived Query Methods)
Spring Data JPA metod nomini ingliz tilidagi kalit so'zlar asosida o'qib, **SQL so'rovni avtomatik o'zi tuzadi**.

> **Muhim qoida:** Metod nomida bazadagi ustun nomi (`user_id`) emas, balki Entity klassingizdagi Java o'zgaruvchi nomi (`userId` -> `UserId`) yoziladi!

#### A) Asosiy boshlanish so'zlari (Prefixes):
* `findBy...` / `findAllBy...` / `getBy...` — Ro'yxat yoki bitta obyekt olish (`SELECT`)
* `findFirstBy...` / `findTopBy...` — Faqat 1-natijani olish (`LIMIT 1`, Laravel: `->first()`)
* `findTop5By...` — Dastlabki 5 tasini olish (`LIMIT 5`)
* `existsBy...` — Bor-yo'qligini tekshirish (`boolean` qaytaradi)
* `countBy...` — Sonini sanash (`long` qaytaradi)
* `deleteBy...` / `removeBy...` — Shart bo'yicha o'chirish (`DELETE`)

#### B) Barcha shart kalit so'zlari va misollar:

```java
package uz.learning.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.learning.Entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. TENGLIK (=)
    // SQL: SELECT * FROM products WHERE name = ?
    Optional<Product> findByName(String name);

    // SQL: SELECT * FROM products WHERE user_id = ?
    List<Product> findAllByUserId(Long userId);

    // 2. NOT EQUAL (!= yoki <>)
    // SQL: SELECT * FROM products WHERE user_id <> ?
    List<Product> findByUserIdNot(Long userId);

    // 3. AND (Va)
    // SQL: SELECT * FROM products WHERE id = ? AND user_id = ?
    Optional<Product> findByIdAndUserId(Long id, Long userId);

    // 4. OR (Yoki)
    // SQL: SELECT * FROM products WHERE name = ? OR price = ?
    List<Product> findByNameOrPrice(String name, Long price);

    // 5. QIDIRUV (LIKE / ILIKE)
    // SQL: WHERE name LIKE '%search%'
    List<Product> findByNameContaining(String search);

    // SQL: WHERE LOWER(name) LIKE LOWER('%search%') (Katta-kichik harf farqsiz + Pagination)
    Page<Product> findByNameContainingIgnoreCase(String search, Pageable pageable);

    // SQL: WHERE name LIKE 'search%' (Shu so'z bilan boshlanadiganlar)
    List<Product> findByNameStartingWith(String prefix);

    // SQL: WHERE name LIKE '%search' (Shu so'z bilan tugaydiganlar)
    List<Product> findByNameEndingWith(String suffix);

    // 6. KATTA / KICHIK (>, >=, <, <=)
    // SQL: WHERE price > ?
    List<Product> findByPriceGreaterThan(Long minPrice);

    // SQL: WHERE price >= ?
    List<Product> findByPriceGreaterThanEqual(Long minPrice);

    // SQL: WHERE price < ?
    List<Product> findByPriceLessThan(Long maxPrice);

    // SQL: WHERE price <= ?
    List<Product> findByPriceLessThanEqual(Long maxPrice);

    // 7. ORALIQ (BETWEEN)
    // SQL: WHERE price BETWEEN ? AND ?
    List<Product> findByPriceBetween(Long minPrice, Long maxPrice);

    // SQL: WHERE created_at BETWEEN ? AND ?
    List<Product> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 8. SANALAR (Before / After)
    // SQL: WHERE created_at < ?
    List<Product> findByCreatedAtBefore(LocalDateTime date);

    // SQL: WHERE created_at > ?
    List<Product> findByCreatedAtAfter(LocalDateTime date);

    // 9. NULL TEKSHIRUVI (IS NULL / IS NOT NULL)
    // SQL: WHERE user_id IS NULL
    List<Product> findByUserIdIsNull();

    // SQL: WHERE user_id IS NOT NULL
    List<Product> findByUserIdIsNotNull();

    // 10. RO'YXAT ICHIDAN QIDIRISH (IN / NOT IN)
    // SQL: WHERE id IN (1, 2, 3)
    List<Product> findByIdIn(Collection<Long> ids);

    // SQL: WHERE id NOT IN (1, 2, 3)
    List<Product> findByIdNotIn(Collection<Long> ids);

    // 11. BOOLEAN USTUNLAR UCHUN (True / False)
    // SQL: WHERE active = true
    // List<Product> findByActiveTrue();
    // List<Product> findByActiveFalse();

    // 12. SARALASH (ORDER BY ... ASC / DESC)
    // SQL: WHERE user_id = ? ORDER BY created_at DESC
    List<Product> findAllByUserIdOrderByCreatedAtDesc(Long userId);

    // SQL: WHERE user_id = ? ORDER BY price ASC
    List<Product> findAllByUserIdOrderByPriceAsc(Long userId);

    // 13. LIMIT (First / Top)
    // SQL: WHERE user_id = ? ORDER BY id DESC LIMIT 1
    Optional<Product> findFirstByUserIdOrderByIdDesc(Long userId);

    // SQL: WHERE user_id = ? ORDER BY price DESC LIMIT 5
    List<Product> findTop5ByUserIdOrderByPriceDesc(Long userId);

    // 14. EXISTS va COUNT va DELETE
    // SQL: SELECT EXISTS(SELECT 1 FROM products WHERE id = ? AND user_id = ?)
    boolean existsByIdAndUserId(Long id, Long userId);

    // SQL: SELECT COUNT(*) FROM products WHERE user_id = ?
    long countByUserId(Long userId);

    // SQL: DELETE FROM products WHERE id = ? AND user_id = ?
    void deleteByIdAndUserId(Long id, Long userId);
}
```

---

### 2-USUL: `@Query` orqali JPQL (Java Persistence Query Language) yozish
Agar shartlar ko'p bo'lsa yoki metod nomi juda uzun bo'lib ketsa, `@Query` ishlatiladi.
> **Eslatma:** JPQL ichida bazadagi jadval nomi (`products`) emas, **Java Entity klass nomi (`Product`)** va Java field nomlari (`p.userId`, `p.createdAt`) yoziladi!

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uz.learning.Entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Oddiy JPQL SELECT so'rovi (:userId va :minPrice parametrlari bilan)
    @Query("SELECT p FROM Product p WHERE p.userId = :userId AND p.price >= :minPrice ORDER BY p.id DESC")
    List<Product> findUserExpensiveProducts(
            @Param("userId") Long userId,
            @Param("minPrice") Long minPrice
    );

    // 2. Qidiruv (LIKE) + Pagination bilan JPQL
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    // 3. JPQL orqali UPDATE qilish (@Modifying va @Transactional bo'lishi SHART!)
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = :newPrice WHERE p.id = :id AND p.userId = :userId")
    int updateProductPrice(
            @Param("id") Long id,
            @Param("userId") Long userId,
            @Param("newPrice") Long newPrice
    );

    // 4. JPQL orqali DELETE qilish
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.id = :id AND p.userId = :userId")
    int deleteUserProduct(@Param("id") Long id, @Param("userId") Long userId);
}
```

---

### 3-USUL: `@Query(nativeQuery = true)` — Toza PostgreSQL SQL yozish
Agar sizga bazaning o'ziga xos imkoniyatlari (`JOIN`, `GROUP BY`, `ILIKE`, murakkab hisob-kitoblar) kerak bo'lsa, `nativeQuery = true` qilasiz va **haqiqiy jadval (`products`, `users`) hamda ustun nomlarini (`user_id`, `created_at`)** yozasiz:

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import uz.learning.Entity.Product;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 1. Toza PostgreSQL SELECT so'rovi
    @Query(
        value = "SELECT * FROM products WHERE user_id = :userId AND count > 0 ORDER BY created_at DESC LIMIT :limit",
        nativeQuery = true
    )
    List<Product> findLatestActiveProducts(
            @Param("userId") Long userId,
            @Param("limit") int limit
    );

    // 2. PostgreSQL ILIKE bilan qidiruv
    @Query(
        value = "SELECT * FROM products WHERE name ILIKE CONCAT('%', :search, '%')",
        nativeQuery = true
    )
    List<Product> searchByNameNative(@Param("search") String search);

    // 3. Ombor qoldig'ini (count) 1 taga kamaytirish (UPDATE)
    @Modifying
    @Transactional
    @Query(
        value = "UPDATE products SET count = count - 1, updated_at = NOW() WHERE id = :id AND count > 0",
        nativeQuery = true
    )
    int decrementCount(@Param("id") Long id);
}
```

---

## 4. Qaysi usulni qachon tanlash kerak?

1. **Tayyor `JpaRepository` metodlari** (`save`, `findById`, `findAll`, `deleteById`) — Standart CRUD amallar uchun.
2. **1-Usul (Metod nomi orqali)** — 1 tadan 3 tagacha oddiy shartli qidiruvlar uchun (`findByUsername`, `findByToken`, `findAllByProductId`). Eng tez va oson yo'l.
3. **2-Usul (`@Query` JPQL)** — Shartlar ko'payib ketganda yoki metod nomi juda uzun bo'lib ketganda.
4. **3-Usul (`nativeQuery = true`)** — Murakkab PostgreSQL so'rovlari, `JOIN`, `GROUP BY`, statistika va katta hajmdagi tezkor so'rovlar uchun.
