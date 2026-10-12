package uz.learning.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import uz.learning.Entity.Message;

@Repository 
public interface MessageRepository extends JpaRepository<Message, Long>{
    List<Message> findAllByProductId(Long productId);
}
