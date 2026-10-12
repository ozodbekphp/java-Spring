package uz.learning.Service;

import java.time.LocalDateTime;
import java.util.List;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uz.learning.Entity.Product;
import uz.learning.Repository.ProductRepository;
import uz.learning.dto.request.ProductRequest;
import uz.learning.dto.response.ProductPaginationResponse;
import uz.learning.dto.response.ProductResponse;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductInterface {

    private final ProductRepository productRepository;

    @Override
    public ProductResponse store(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .count(request.getCount())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

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
    }

    public ProductPaginationResponse index(String search)
    {
        Page<Product> products;
        Pageable pageable = PageRequest.of(0, 10);

        if(search != null && !search.isBlank()){
            products = productRepository.findBynameContainingIgnoreCase(search,  pageable);
        }else{
            products = productRepository.findAll(pageable);
        }
        
        List<ProductResponse> productList = products.getContent().stream()
            .map(p -> ProductResponse.builder()
                    .id(p.getId())
                    .name(p.getName())
                    .count(p.getCount())
                    .price(p.getPrice())
                    .createdAt(p.getCreatedAt())
                    .updatedAt(p.getUpdatedAt())
                    .build())
            .toList();

        return  ProductPaginationResponse.builder()
                .products(productList)
                .CurrentPage(products.getNumber())
                .totalPage(products.getTotalPages())
                .totalElements(products.getTotalElements())
                .build();
    }

    @Override
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi, id: " + id));

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setCount(request.getCount());
        product.setUpdatedAt(LocalDateTime.now());

        Product updatedProduct = productRepository.save(product);

        return ProductResponse.builder()
                .id(updatedProduct.getId())
                .name(updatedProduct.getName())
                .price(updatedProduct.getPrice())
                .count(updatedProduct.getCount())
                .createdAt(updatedProduct.getCreatedAt())
                .updatedAt(updatedProduct.getUpdatedAt())
                .message("Mahsulot muvaffaqiyatli yangilandi")
                .build();
    }

    public ProductResponse delete(Long id){
        Product product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Mahsulot topilmadi, id: " + id));

        productRepository.delete(product);

        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .count(product.getCount())
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .message("Mahsulot muvaffaqiyatli o'chirildi")
            .build();
    }
}
