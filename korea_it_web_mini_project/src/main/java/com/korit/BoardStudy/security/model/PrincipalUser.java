package com.korit.BoardStudy.security.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.korit.BoardStudy.entity.UserRole;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class PrincipalUser implements UserDetails {

    private Integer userId;
    private String username;
    @JsonIgnore
    private String password;
    private String email;
    private List<UserRole> userRoles;

    //userRole(rolename 가져와서) 통해 Simple~ (권한 객체)를 만드는 것
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userRoles.stream().map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName()))
                .collect(Collectors.toList());
    }
}
