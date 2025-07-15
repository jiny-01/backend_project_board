package com.korit.BoardStudy.mapper;

import com.korit.BoardStudy.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

@Mapper
public interface UserMapper {

    Optional<User> getUserByUserId(Integer userId);
    Optional<User> getUserByUserName(String username);
    Optional<User> getUserByUserEmail(String useremail);


    int addUser(User user);

    int updatePassword(User user);
}
