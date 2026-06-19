package com.dvein.banking_backend.auth.repository;

import com.dvein.banking_backend.auth.model.Permission;
import com.dvein.banking_backend.auth.model.Role;
import com.dvein.banking_backend.auth.model.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {

    List<RolePermission> findByRole(Role role);

    boolean existsByRoleAndPermission(Role role, Permission permission);

    void deleteByRoleAndPermission(Role role, Permission permission);
}