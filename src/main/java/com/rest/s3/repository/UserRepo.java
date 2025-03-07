package com.rest.s3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rest.s3.model.UserData;

public interface UserRepo extends JpaRepository<UserData, Long> {

	boolean existsByUsernameIgnoreCase(String username);
//	UserData findByUsername(String username);

	boolean existsByEmailIgnoreCase(String email);

	UserData findByUsernameIgnoreCase(String username);

	UserData findByEmailIgnoreCase(String email);

}
