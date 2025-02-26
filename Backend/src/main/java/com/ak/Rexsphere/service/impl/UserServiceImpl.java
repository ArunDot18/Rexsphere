package com.ak.Rexsphere.service.impl;

import com.ak.Rexsphere.service.JWTService;
import com.ak.Rexsphere.entity.User;
import com.ak.Rexsphere.repository.UserRepository;
import com.ak.Rexsphere.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User getUserByUserName(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUser(Long id, User updatedUser) {
        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User existingUser = user.get();

            if (updatedUser.getFirstName() != null) existingUser.setFirstName(updatedUser.getFirstName());
            if (updatedUser.getLastName() != null) existingUser.setLastName(updatedUser.getLastName());
            if (updatedUser.getUserName() != null) existingUser.setUserName(updatedUser.getUserName());
            if (updatedUser.getEmail() != null) existingUser.setEmail(updatedUser.getEmail());
            if (updatedUser.getPassword() != null) existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            if (updatedUser.getMobileNumber() != null) existingUser.setMobileNumber(updatedUser.getMobileNumber());
            if (updatedUser.getAddress() != null) existingUser.setAddress(updatedUser.getAddress());
            if (updatedUser.getDateOfBirth() != null) existingUser.setDateOfBirth(updatedUser.getDateOfBirth());

            return userRepository.save(existingUser);
        } else {
            return null;
        }
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public String verify(User user) {
        User userFromDb = userRepository.findByEmail(user.getEmail());

        if (userFromDb == null) {
            return "User not found.";
        }

        if (!"LOCAL".equals(userFromDb.getProvider())) {
            return "Use OAuth2 to log in.";
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword())
        );

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getEmail(), userFromDb.getId());
        }
        return "Authentication Failed.";
    }

    @Override
    public void updateProfilePictureUrl(Long id, String newImageUrl) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setProfilePictureUrl(newImageUrl);
            userRepository.save(user);
        }
    }

    @Override
    public ResponseEntity<String> setPassword(String password, String token) {
        String email = jwtService.extractEmail(token.substring(7));
        User user = userRepository.findByEmail(email);

        if (user == null || !"OAUTH2".equals(user.getProvider())) {
            return ResponseEntity.status(403).body("User not found or not an OAuth2 user.");
        }

        user.setPassword(passwordEncoder.encode(password));
        user.setProvider("LOCAL");
        userRepository.save(user);

        return ResponseEntity.ok("Password set successfully. Uou can now log in with email and password.");
    }
}
