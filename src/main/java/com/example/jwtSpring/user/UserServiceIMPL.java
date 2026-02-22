package com.example.jwtSpring.user;

import com.example.jwtSpring.exception.ResourceNotFoundException;
import com.example.jwtSpring.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceIMPL implements UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceIMPL(UserRepo userRepo, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepo = userRepo;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User addUser(User user) {

        // 1. Validation: Check if email is already taken
        if (userRepo.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User with email " + user.getEmail() + " already exists.");
        }

        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

        if (user.getProvider() == null) {
            user.setProvider("local");
            user.setRoles("USER");
        }

        return userRepo.save(user);
    }

    @Override
    public User getUserById(Long id) {
        // Validation: Handle case where ID does not exist instead of using .get()
        return userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public void deleteUser(Long id) {
        // Validation: Check existence before deleting to avoid silent failures
        if (!userRepo.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. User not found with id: " + id);
        }
        userRepo.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }
}