package uz.learning.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import uz.learning.Service.ProductInterface;
import uz.learning.dto.request.ProductRequest;
import uz.learning.dto.response.ProductPaginationResponse;
import uz.learning.dto.response.ProductResponse;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor // Bu o'zidan constuctor yarataadi va final bilan yaratigan o'zgaruvchilarga
                         // avtomatik constructor yaratadi va @NotNull uchun ham kiradi.
public class ProductController {

    private final ProductInterface productService;

    @PostMapping
    public ResponseEntity<ProductResponse> store(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.store(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ProductPaginationResponse> index(
        @RequestParam(required = false) String search
    ){
        ProductPaginationResponse response = productService.index(search);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        ProductResponse response = productService.update(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponse> delete(
        @PathVariable Long id
    ) {
        ProductResponse response = productService.delete(id);
        return ResponseEntity.ok(response);
    }
}
