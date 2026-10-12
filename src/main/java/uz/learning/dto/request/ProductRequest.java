package uz.learning.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class ProductRequest {
    // @NotBlank faqat string uchun ishlatiladi.
    @NotBlank (message = "Mahsulot nom kirtish majburiy")
    private String name;

    // Sonlar uchun odatda @NotNull va @Positve yoki @Min ishlatiladi
    @NotNull (message = "Mahsulot narx kiritish majburiy")
    @Positive (message = "Mahsulot narx 0 dan  katta bo'lishi kerak.")
    private Long price;

    @NotNull(message = "Mahsulot soni bo'sh bo'lmasligi kerak")
    private Integer count;
}
