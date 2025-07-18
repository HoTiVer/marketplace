package org.hotiver.repo;

import org.hotiver.domain.Entity.Message;
import org.hotiver.domain.Entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepo extends JpaRepository<Message, Long> {

    List<Message> findAllByChatOrderBySentAtAsc(Chat chat);

}
