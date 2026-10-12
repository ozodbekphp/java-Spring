package uz.learning.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter 
@Getter  
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class AuthResponse {
    private String token;
}
