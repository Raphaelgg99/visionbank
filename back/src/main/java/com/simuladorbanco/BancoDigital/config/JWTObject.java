package com.simuladorbanco.BancoDigital.config;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class JWTObject {
    private String subject; // nome do usuario
    private Date issuedAt; // data de criação do token
    private Date expiration; // data de expiração do token
    private List<String> roles; // perfis de acesso

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public Date getExpiration() {
        return expiration;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public Date getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Date issuedAt) {
        this.issuedAt = issuedAt;
    }
}
