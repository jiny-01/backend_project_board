package com.korit.BoardStudy.repository;

import com.korit.BoardStudy.entity.User;
import com.korit.BoardStudy.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

    @Autowired
    private UserMapper userMapper;

    public Optional<User> getUserByUserId(Integer userId) {
        return userMapper.getUserByUserId(userId);
    }

    public Optional<User> getUserByUserName(String username) {
        return userMapper.getUserByUserName(username);
    }

    public Optional<User> getUserByUserEmail(String email) {
        return userMapper.getUserByUserEmail(email);
    }

    public Optional<User> addUser(User user) {
        try {
            userMapper.addUser(user);
        } catch (DuplicateKeyException e) {
            return Optional.empty();
        }

        return Optional.of(user);
    }

    public int changePassword(User user) {
        return userMapper.updatePassword(user);
    }




}
