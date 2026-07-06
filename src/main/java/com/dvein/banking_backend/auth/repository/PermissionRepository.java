package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByName(String name);

    List<Permission> findByModule(String module);

    boolean existsByName(String name);
}