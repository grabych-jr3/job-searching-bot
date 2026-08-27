package com.ogidazepam.job_api_service.auth.service;

import com.ogidazepam.job_api_service.auth.model.dto.LoginRequest;
import com.ogidazepam.job_api_service.auth.model.dto.SignUpRequest;
import com.ogidazepam.job_api_service.auth.model.entity.Customer;
import com.ogidazepam.job_api_service.auth.model.enums.CustomerRole;
import com.ogidazepam.job_api_service.auth.repository.CustomerRepository;
import com.ogidazepam.job_api_service.auth.util.CustomUserDetails;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final AuthenticationManager authenticationManager;

    public CustomerService(JwtService jwtService, PasswordEncoder passwordEncoder, CustomerRepository customerRepository, AuthenticationManager authenticationManager) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public void saveCustomer(SignUpRequest dto){
        if (customerRepository.existsByEmail(dto.email())){
            throw new DataIntegrityViolationException("This email was already registered");
        }
        customerRepository.save(toCustomer(dto));
    }

    public String login(LoginRequest dto){
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.email(),
                        dto.password()
                )
        );

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

    private Customer toCustomer(SignUpRequest dto){
        return Customer.builder()
                .email(dto.email())
                .password(passwordEncoder.encode(dto.password()))
                .roles(List.of(CustomerRole.USER))
                .build();
    }
}
