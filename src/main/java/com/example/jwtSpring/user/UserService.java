package com.example.jwtSpring.user;


import java.util.*;

public interface UserService {

    User addUser(User user);

    void deleteUser(Long id);

    User getUserById(Long id);

    List<User> getAllUsers();

}
