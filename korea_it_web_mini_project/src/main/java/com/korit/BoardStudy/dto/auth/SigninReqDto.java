package com.korit.BoardStudy.dto.auth;

import com.korit.BoardStudy.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SigninReqDto {
    private String username;
    private String password;


}
