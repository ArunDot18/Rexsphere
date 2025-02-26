package com.ak.Rexsphere.service;

import com.ak.Rexsphere.entity.User;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User getUserById(Long id);
    User getUserByEmail(String email);
    User getUserByUserName(String userName);
    List<User> getAllUsers();
    User updateUser(Long id, User updatedUser);
    void deleteUser(Long id);
    String verify(User user);
    void updateProfilePictureUrl(Long id, String newImageUrl);
    ResponseEntity<String> setPassword(String password, String token);
}
