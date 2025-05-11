package com.mtech.webapp.security;

import com.mtech.webapp.models.Role;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomAuthenticationProvider extends DaoAuthenticationProvider {

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        Role role = (Role) authentication.getDetails();

        // Call your custom method here
        UserDetails userDetails = ((JwtUserDetailsService)this.getUserDetailsService()).loadUserByUserIdAndRole(username,role);

        // Perform the usual authentication checks using the UserDetails
        if (userDetails != null && getPasswordEncoder().matches(password, userDetails.getPassword())) {
            return new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        } else {
            throw new BadCredentialsException("Login Failed due to Invalid username or password");
        }
    }
}

