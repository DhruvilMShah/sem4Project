package com.mtech.webapp.security;


import com.mtech.webapp.models.Role;
import com.mtech.webapp.models.User;
import com.mtech.webapp.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class JwtUserDetailsService implements UserDetailsService{

    final UserRepository userRepository;
    final JwtTokenUtil jwtTokenUtil;

    public JwtUserDetailsService(UserRepository userRepository, JwtTokenUtil jwtTokenUtil) {
        this.userRepository = userRepository;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public UserDetails loadUserByUserIdAndRole(String userId, Role role) throws UsernameNotFoundException {
        String userIdFromRepository = null;
        String passwordFromRepository = null;

        //Get the user details based on the role from the Database
        if (Objects.requireNonNull(role) == Role.USER) {
            User user = userRepository.findByUserId(userId);
            if (user != null) {
                userIdFromRepository = user.getUserId();
                passwordFromRepository = user.getPasswordHash();
            }
        } else {
            throw new IllegalStateException("Unexpected value: " + role);
        }

        // Add Applicable role to the user
        List<GrantedAuthority> authorityList = new ArrayList<>();
        authorityList.add(new SimpleGrantedAuthority("ROLE_"+role.name()));

        //Return UserDetails if found else return NULL
        return userIdFromRepository != null ?  new org.springframework.security.core.userdetails.User(userIdFromRepository, passwordFromRepository, authorityList) : null;
    }

    @Deprecated
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
