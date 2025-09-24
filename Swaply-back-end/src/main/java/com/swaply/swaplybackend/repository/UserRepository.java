package com.swaply.swaplybackend.repository;

import com.swaply.swaplybackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>{

    Optional<User> findByUserName(String userName);
    Optional<User> findByEmail(String email);

    boolean existsByUserName(String userName);
    boolean existsByEmail(String email);

    Optional<User> findByUserNameAndEmail(String userName, String email);
}
