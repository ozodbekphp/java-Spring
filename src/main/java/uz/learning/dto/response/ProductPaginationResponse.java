package uz.learning.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class ProductPaginationResponse {
    private List<ProductResponse> products;
    private int CurrentPage;
    private int totalPage;
    private long totalElements;
}
