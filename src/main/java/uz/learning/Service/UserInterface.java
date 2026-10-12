package uz.learning.Service;

import uz.learning.dto.request.UserRequest;
import uz.learning.dto.response.AuthResponse;
import uz.learning.dto.response.UserResponse;

public interface UserInterface {
    UserResponse register(UserRequest request);
    AuthResponse login(UserRequest request);  
}
