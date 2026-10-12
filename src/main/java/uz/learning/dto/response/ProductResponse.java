package uz.learning.dto.response;

import java.time.LocalDateTime;

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
public class ProductResponse {
    private Long id;
    private String name;
    private Long price;
    private Integer count;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String message;
}
