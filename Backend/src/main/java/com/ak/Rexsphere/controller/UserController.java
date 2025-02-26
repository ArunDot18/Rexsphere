package com.ak.Rexsphere.controller;

import com.ak.Rexsphere.entity.User;
import com.ak.Rexsphere.service.CloudinaryService;
import com.ak.Rexsphere.service.UserService;
import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private final CloudinaryService cloudinaryService;

    public UserController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(){
        List<User> all = userService.getAllUsers();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id){
        User user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/username/{userName}")
    public ResponseEntity<User> getUserByUserName(@PathVariable String userName){
        User user = userService.getUserByUserName(userName);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User updatedUser) {
        User user = userService.updateUser(id, updatedUser);
        return new ResponseEntity<>(user, HttpStatus.ACCEPTED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }

    @PostMapping("/upload-profile-picture")
    public ResponseEntity<String> uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getCredentials();
        User user = userService.getUserById(userId);
        try {
            String oldImageUrl = user.getProfilePictureUrl();
            String newImageUrl = cloudinaryService.uploadImage(file);

            if (oldImageUrl != null && !oldImageUrl.isEmpty()){
                cloudinaryService.deleteImage(oldImageUrl);
            }
            userService.updateProfilePictureUrl(user.getId(), newImageUrl);
            return ResponseEntity.ok("Profile picture updated successfully: " + newImageUrl);

        } catch (IOException e) {
            return ResponseEntity.status(500).body("Image upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/set-password")
    public ResponseEntity<String> setPassword(@RequestParam("password") String password,
                                              @RequestHeader("Authorization") String token) {
        return userService.setPassword(password, token);
    }

}
