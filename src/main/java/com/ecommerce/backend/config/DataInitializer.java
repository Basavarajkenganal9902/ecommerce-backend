package com.ecommerce.backend.config;

import com.ecommerce.backend.entity.Admin;
import com.ecommerce.backend.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Value("${ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD}")
    private String adminPassword;

    @Bean
    CommandLineRunner createAdmin(
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            if (adminRepository.findByEmail(adminEmail).isEmpty()) {

                Admin admin = new Admin();

                admin.setName("Admin");
                admin.setEmail(adminEmail);

                admin.setPassword(
                        passwordEncoder.encode(adminPassword)
                );

                admin.setRole("ADMIN");

                adminRepository.save(admin);

                System.out.println("Default admin created.");
            }
        };
    }
}