package com.shopease.shopease_backend.controller;



import com.shopease.shopease_backend.entity.Role;
import com.shopease.shopease_backend.entity.User;
import com.shopease.shopease_backend.repository.UserRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private final UserRepository userRepository;

    public TestController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/create-user")
    public User createUser() {
        User user = new User();
        user.setName("Dinesh Test");
        user.setEmail("dinesh@test.com");
        user.setPassword("password123");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}