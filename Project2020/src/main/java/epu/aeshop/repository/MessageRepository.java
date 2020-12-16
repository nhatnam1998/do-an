package epu.aeshop.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Message;

@Repository
public interface MessageRepository extends CrudRepository<Message, Long> {
}
