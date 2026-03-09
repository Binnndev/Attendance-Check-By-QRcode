package com.attendance.backend.auth.repository;

import com.attendance.backend.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
        select u
        from User u
        where lower(u.email) = :emailNorm
          and u.deletedAt is null
    """)
    Optional<User> findForLogin(@Param("emailNorm") String emailNorm);
}