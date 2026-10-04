package com.ecommerce.service;

import com.ecommerce.dto.RegistrationDto;
import com.ecommerce.model.Role;
import com.ecommerce.model.User;
import com.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Called by Spring Security at login. The "username" is the email address. */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User u = userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
        return org.springframework.security.core.userdetails.User.withUsername(u.getEmail())
                .password(u.getPassword())
                .roles(u.getRole().name())
                .disabled(!u.isEnabled())
                .build();
    }

    @Transactional
    public User register(RegistrationDto dto) {
        User u = new User();
        u.setFirstName(dto.getFirstName().trim());
        u.setLastName(dto.getLastName().trim());
        u.setEmail(dto.getEmail().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setRole(Role.USER);
        return userRepository.save(u);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email.trim().toLowerCase());
    }

    public Optional<User> findByEmail(String email) { return userRepository.findByEmail(email); }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public List<User> findAll() { return userRepository.findAll(); }

    @Transactional
    public void updateName(User user, String first, String last) {
        user.setFirstName(first.trim());
        user.setLastName(last.trim());
        userRepository.save(user);
    }

    @Transactional
    public void toggleEnabled(Long id) {
        User u = userRepository.findById(id).orElseThrow();
        u.setEnabled(!u.isEnabled());
    }
}
