package uz.learning.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import uz.learning.Service.MessageInterface;
import uz.learning.dto.request.MessageRequest;
import uz.learning.dto.response.MessageResponse;

@RestController 
@RequestMapping("/api/message")
@RequiredArgsConstructor 
public class MessageController {
    private final MessageInterface messageService;

    @PostMapping
    public ResponseEntity<MessageResponse> store(MessageRequest request)
    {
        MessageResponse response = messageService.store(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<List<MessageResponse>> index(
        @PathVariable Long id
    ){
        List<MessageResponse> response = messageService.index(id);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(
        @PathVariable Long id,
        @RequestBody MessageRequest request
    ){
        MessageResponse response = messageService.update(request, id);
        return ResponseEntity.ok(response);  
    }

    
}
