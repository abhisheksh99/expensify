package com.abhishek.expensify.service;

import com.abhishek.expensify.entity.ProfileEntity;
import com.abhishek.expensify.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final ProfileRepository profileRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        ProfileEntity existingUser = profileRepository.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("Profile not found with email: "+email));
        return User.builder()
                .username(existingUser.getEmail())
                .password(existingUser.getPassword())
                .authorities(Collections.emptyList())
                .build();


    }
}
