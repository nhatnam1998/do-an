package epu.aeshop.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import epu.aeshop.entity.Message;
import epu.aeshop.entity.User;

import java.util.List;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    // find user by email
	@Query(value = "Select u from User u Where u.email =:email")
    User findByEmail(@Param("email") String email);

    // get last 5 unread notify message of user by email.
    @Query(value =
        "select m.id, m.content"
        + " from message m join user u on m.user_id = u.id"
        + " where m.read = 0 and u.email = ?1"
        + " order by m.received_date desc",
    nativeQuery = true)
    List<Message> getLast5UnreadNotifyMessageByUserEmail(String email);

}
