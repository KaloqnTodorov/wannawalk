package com.wannawalk.backend.repository;

import com.wannawalk.backend.model.ChatMessage;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    @Query("{$or: [ { $and: [ { from: ?0 }, { to: ?1 } ] }, { $and: [ { from: ?1 }, { to: ?0 } ] } ] }")
    List<ChatMessage> findConversationBetween(String userId, String friendId, Sort sort);
}
