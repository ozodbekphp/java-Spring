package uz.learning.Service;

import java.util.List;

import uz.learning.dto.request.MessageRequest;
import uz.learning.dto.response.MessageResponse;

public interface MessageInterface {
    MessageResponse store(MessageRequest request);
    List<MessageResponse> index(Long id);
    MessageResponse update(MessageRequest request, Long id);
    MessageResponse delete(Long id);
} 
