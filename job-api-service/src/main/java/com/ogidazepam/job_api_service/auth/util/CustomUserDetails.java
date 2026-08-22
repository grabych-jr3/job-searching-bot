package com.ogidazepam.job_api_service.auth.util;

import com.ogidazepam.job_api_service.auth.model.entity.Customer;
import com.ogidazepam.job_api_service.auth.model.enums.CustomerRole;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public record CustomUserDetails(Customer customer) implements UserDetails {
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return customer.getRoles().stream()
                .map(CustomerRole::toAuthority)
                .toList();
    }

    @Override
    public @Nullable String getPassword() {
        return customer.getPassword();
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    public Long getCustomerId() { return customer.getId(); }
}
