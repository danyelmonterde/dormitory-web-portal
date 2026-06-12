package com.example.dormportal.repository;

import com.example.dormportal.model.User;
import com.example.dormportal.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRoomId(Long roomId);
    List<User> findByRole(Role role);
}
