package com.spring.Services.ServiceImplements;

import com.spring.Entity.User;
import com.spring.Repository.UserRepository;
import com.spring.Services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Loading user: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("User not found: " + username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        System.out.println("User found: " + user.getUsername() + ", enabled: " + user.isEnabled());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(getAuthorities(user))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(!user.isEnabled()) // Important: check if user is enabled
                .build();
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            System.out.println("No roles found for user: " + user.getUsername());
            return java.util.Collections.emptySet();
        }

        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toSet());

        System.out.println("Authorities for user " + user.getUsername() + ": " + authorities);
        return authorities;
    }
}