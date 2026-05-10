package com.koleksiyon.envanter.service;

import com.koleksiyon.envanter.entity.User;
import com.koleksiyon.envanter.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;

    // Spring Security'nin giriş yaparken kullanıcıyı veritabanında bulması için gereken zorunlu metot
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRole()) // ROLE_ADMIN veya ROLE_USER'ı doğrudan veritabanından alıyor
                .build();
    }

    // Yeni kullanıcı sisteme kayıt olurken (Register) çalışacak metot
    public void saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("ROLE_USER"); // Dışarıdan kaydolan herkes normal kullanıcıdır
        userRepository.save(user);
    }

    // Kullanıcı adı ile kendi User nesnemizi bulmak için yardımcı metot
    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    // Sistem başladığında otomatik admin oluşturma
    @PostConstruct
    public void initAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Admin şifresi: admin123
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
        }
    }

    @Transactional
    public void updateUser(User user) {
        userRepository.save(user);
    }

    public java.util.List<User> getAllUsers() {
        return userRepository.findAll();
    }
}