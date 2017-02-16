package com.niit.blogmiddleend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.niit.blogbackend.model.User;
import com.niit.blogbackend.dao.UserDAO;

@RestController
public class TUserController {
	@Autowired
	UserDAO userDAO;
	@Autowired
	User user;
@GetMapping("/getAll")
	public ResponseEntity<List<User>> getAll() {
		List users = userDAO.getAll();
		if (users.isEmpty()) {
			user.setErrorCode("100");
			user.setErrorMessage("No Users are available");
			users.add(user);
			return new ResponseEntity<List<User>>(users, HttpStatus.OK);
		}
		user.setErrorCode("200");
		user.setErrorMessage("Successfully fetched the user");
		return new ResponseEntity<List<User>>(users, HttpStatus.OK);
	}
@PostMapping("/createUser")
public ResponseEntity<User> createUser(@RequestBody User user){
	if(userDAO.getById(user.getId())==null){
		userDAO.save(user);
		user.setErrorCode("200");
		user.setErrorMessage("user created successfully");
		
		
	}
	else{
		user.setErrorCode("301");
		user.setErrorMessage("already esist"+user.getId());
	}
	return new ResponseEntity<User>(user,HttpStatus.CREATED);
}
}
