package com.ogidazepam.job_api_service.auth.model.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

public enum CustomerRole {
    USER,
    ADMIN;

    public SimpleGrantedAuthority toAuthority(){
        return new SimpleGrantedAuthority("ROLE_" + this.name());
    }
}
