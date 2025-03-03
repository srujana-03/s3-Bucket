package com.rest.s3.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rest.s3.model.UserData;
import com.rest.s3.repository.UserRepo;

@RestController
@RequestMapping("/api/files")
public class UserController {
	private final UserRepo userRepo;

	public UserController(UserRepo userRepo) {
		
		this.userRepo = userRepo;
	}
	
	@PostMapping("addUser")
	public UserData addUser(@RequestBody UserData user) {
		UserData save=userRepo.save(user);
		return save;
	}
	

}
