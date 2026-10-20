package com.eswar.uscistracker.service;

import com.eswar.uscistracker.domain.AppUser;
import com.eswar.uscistracker.repository.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;

    public AppUserDetailsService(AppUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        AppUser user = users.findByUsernameIgnoreCase(login)
            .or(() -> users.findByEmailIgnoreCase(login))
            .orElseThrow(() -> new UsernameNotFoundException("Invalid username/email or password."));

        return User.withUsername(user.getUsername())
            .password(user.getPasswordHash())
            .roles(user.getRole())
            .disabled(!user.isEnabled())
            .build();
    }
}
