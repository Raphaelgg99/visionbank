package com.simuladorbanco.BancoDigital.repository;

import com.simuladorbanco.BancoDigital.model.Conta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.security.authentication.jaas.JaasPasswordCallbackHandler;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContaRepository extends JpaRepository<Conta, Long> {
    @Query("SELECT c FROM Conta c JOIN FETCH c.roles WHERE c.email= (:email)")
    Conta findByEmail(@Param("email") String email);
    boolean existsBySenha(String senha);
    boolean existsByEmail(String email);
    @Query("SELECT c FROM Conta c WHERE c.senha = :senha")
    Conta findBySenha(@Param("senha") String senha);
}
