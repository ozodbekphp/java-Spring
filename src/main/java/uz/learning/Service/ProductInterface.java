package uz.learning.Service;

import uz.learning.dto.request.ProductRequest;
import uz.learning.dto.response.ProductPaginationResponse;
import uz.learning.dto.response.ProductResponse;

public interface ProductInterface {
    ProductResponse store(ProductRequest request);
    ProductPaginationResponse index(String search);
    ProductResponse update(Long id, ProductRequest request);
    ProductResponse delete(Long id);
}
