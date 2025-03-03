package com.rest.s3.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rest.s3.model.UserData;

public interface UserRepo extends JpaRepository<UserData, Long> {
//	UserData findByUsername(String username);

}
