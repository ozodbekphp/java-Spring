# java response method

// 1. 200 OK — Oddiy muvaffaqiyatli javob
return ResponseEntity.ok(userResponse);

// 2. 201 CREATED — Yangi ma'lumot yaratilganda (URI bilan yoki URIsiz)
URI location = ServletUriComponentsBuilder.fromCurrentRequest()
        .path("/{id}").buildAndExpand(user.getId()).toUri();
return ResponseEntity.created(location).body(userResponse);

// 3. 204 NO CONTENT — Ma'lumot muvaffaqiyatli o'chirildi (body qaytarilmaydi)
return ResponseEntity.noContent().build();

// 4. 202 ACCEPTED — So'rov qabul qilindi, lekin fonda (asinxron) bajarilmoqda
return ResponseEntity.accepted().body("Jarayon boshlandi");

// 5. 400 BAD REQUEST — Noto'g'ri so'rov
return ResponseEntity.badRequest().body("Xatolik xabari");

// 6. Ixtiyoriy status kodi (masalan, 404 NOT FOUND, 403 FORBIDDEN, 500)
return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Foydalanuvchi topilmadi");
return ResponseEntity.status(HttpStatus.FORBIDDEN).build();




// Ro'yxat (List) qaytarish
public ResponseEntity<List<UserResponse>> getAllUsers() { ... }

// Sahifalash (Pagination) natijasi
public ResponseEntity<Page<UserResponse>> getUsersPage(Pageable pageable) { ... }

// Body-siz qaytarish (Void)
public ResponseEntity<Void> deleteUser(@PathVariable Long id) { ... }

// Fayl yoki Rasm (byte[] yoki Resource) yuklab olish uchun
public ResponseEntity<Resource> downloadFile() {
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"doc.pdf\"")
            .contentType(MediaType.APPLICATION_PDF)
            .body(fileResource);
}

// Kalit-qiymat ko'rinishida (Map)
public ResponseEntity<Map<String, Object>> getCustomData() { ... }