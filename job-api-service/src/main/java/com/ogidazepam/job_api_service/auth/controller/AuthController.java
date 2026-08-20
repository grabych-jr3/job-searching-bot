package com.ogidazepam.job_api_service.auth.controller;

import com.ogidazepam.job_api_service.auth.model.dto.LoginRequest;
import com.ogidazepam.job_api_service.auth.model.dto.SignUpRequest;
import com.ogidazepam.job_api_service.auth.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final long jwtValidity;
    private final CustomerService customerService;

    public AuthController(@Value("${jwt.expiration}") long jwtValidity, CustomerService customerService) {
        this.jwtValidity = jwtValidity;
        this.customerService = customerService;
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody SignUpRequest dto){
        customerService.saveCustomer(dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest dto){
        String jwt = customerService.login(dto);

        ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(Duration.ofMillis(jwtValidity))
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(){
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }
}
