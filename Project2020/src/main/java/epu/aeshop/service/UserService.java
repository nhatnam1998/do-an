package epu.aeshop.service;

import java.util.List;

import epu.aeshop.entity.Message;
import epu.aeshop.entity.User;

public interface UserService {

    Boolean validatePassword(String password, User user);

    User addUser(User user);

    User updateUser(User user);

    User changePassword(String newPassword, User user);

    User findByEmail(String email);

    List<Message> getLast5UnreadNotifyMessageByUserEmail(String email);
}
