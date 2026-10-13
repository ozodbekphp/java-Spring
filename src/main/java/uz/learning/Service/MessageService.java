package uz.learning.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import uz.learning.Entity.Message;
import uz.learning.Repository.MessageRepository;
import uz.learning.Repository.ProductRepository;
import uz.learning.dto.request.MessageRequest;
import uz.learning.dto.response.MessageResponse;
import uz.learning.filter.AuthContext;

@Service
@RequiredArgsConstructor
public class MessageService implements MessageInterface {

    public final ProductRepository productReposiutory;
    public final MessageRepository messageRepository;

    public MessageResponse store(MessageRequest request) {
        if (!productReposiutory.existsById(request.getProductId())) {
            throw new RuntimeException("Mahsulot id topilmadi: " + request.getProductId());
        }

        // Login qilgan userning ID sini olamiz
        Long currentUserId = AuthContext.getCurrentUserId();

        Message message = Message.builder()
                .message(request.getMessage())
                .productId(request.getProductId())
                .userId(currentUserId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Message createMessage = messageRepository.save(message);

        return MessageResponse.builder()
                .id(createMessage.getId())
                .message("Muvaffaqiyatli yaratildi")
                .createdAt(createMessage.getCreatedAt())
                .updatedAt(createMessage.getUpdatedAt())
                .build();
    }

    public List<MessageResponse> index(Long id) {
        if (!productReposiutory.existsById(id)) {
            throw new RuntimeException("Mahsulot topilmadi, id: " + id);
        }

        List<Message> messages = messageRepository.findAllByProductId(id);

        return messages.stream()
                .map(m -> MessageResponse.builder()
                        .id(m.getId())
                        .message(m.getMessage())
                        .createdAt(m.getCreatedAt())
                        .updatedAt(m.getUpdatedAt())
                        .build())
                .toList();
    }

    public MessageResponse update(MessageRequest request, Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bunday message topilmadi id: " + id));

        // Login qilgan user faqat o'zi yozgan xabarni yangilay oladi
        Long currentUserId = AuthContext.getCurrentUserId();
        if (message.getUserId() == null || !message.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bu xabar sizga tegishli emas! Faqat o'zingiz yozgan xabarni yangilay olasiz.");
        }

        message.setMessage(request.getMessage());
        message.setUpdatedAt(LocalDateTime.now());

        Message updatMessage = messageRepository.save(message);

        return MessageResponse.builder()
                .id(updatMessage.getId())
                .message("Muvaffaqiyatli yangilandi.")
                .time(LocalDateTime.now())
                .build();
    }

    public MessageResponse delete(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Xabar topilmadi."));

        // Login qilgan user faqat o'zi yozgan xabarni o'chira oladi
        Long currentUserId = AuthContext.getCurrentUserId();
        if (message.getUserId() == null || !message.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bu xabar sizga tegishli emas! Faqat o'zingiz yozgan xabarni o'chira olasiz.");
        }

        messageRepository.delete(message);

        return MessageResponse.builder()
                .message("Muvaffaqiyatli o'chirildi.")
                .build();
    }
}
