package com.quizzard.app.repository;

import com.quizzard.app.domain.entity.Role;
import com.quizzard.app.domain.enums.UserRoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRole(UserRoleEnum role);
}
