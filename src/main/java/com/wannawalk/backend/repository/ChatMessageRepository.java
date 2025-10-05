package com.wannawalk.backend.repository;

import com.wannawalk.backend.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{$or: [ { $and: [ { from: ?0 }, { to: ?1 } ] }, { $and: [ { from: ?1 }, { to: ?0 } ] } ] }")
    Page<ChatMessage> findConversationBetween(String userId1, String userId2, Pageable pageable);
}
