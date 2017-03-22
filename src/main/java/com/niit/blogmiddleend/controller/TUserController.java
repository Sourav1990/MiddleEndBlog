package com.niit.blogmiddleend.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.niit.blogbackend.model.Friend;
import com.niit.blogbackend.model.User;
import com.niit.blogbackend.dao.FriendDAO;
import com.niit.blogbackend.dao.UserDAO;

@RestController
public class TUserController {
	private static final Logger logger = LoggerFactory.getLogger(TUserController.class);
	@Autowired
	UserDAO userDAO;
	@Autowired
	User user;
	@Autowired
	Friend friend;
	@Autowired
	HttpSession session;
	@Autowired
	FriendDAO friendDAO;
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
	public ResponseEntity<User> createUser(@RequestBody User user) {
		if (userDAO.getById(user.getId()) == null) {
			user.setIs_online('N');
			userDAO.save(user);
			
			user.setErrorCode("200");
			user.setErrorMessage("user created successfully");

		} else {
			user.setErrorCode("301");
			user.setErrorMessage("already exist" + user.getId());
		}
		return new ResponseEntity<User>(user, HttpStatus.CREATED);
	}
	
	@GetMapping("/getUser/{id}")
	public ResponseEntity<User> getUser(@PathVariable("id") String id) {
		user = userDAO.getById(id);

		if (user == null) {
			user = new User();
			user.setErrorCode("404");
			user.setErrorMessage("No user found with this id :" + id);
		}

		return new ResponseEntity<User>(user, HttpStatus.OK);

	}
	@PostMapping("/tValidate/")
	public ResponseEntity<User> validateCredentials(@RequestBody User user,HttpSession httpSession){

		user = userDAO.authenticate(user.getId(), user.getPassword());

		if (user == null) { 
			// NLP NullPointerException if we dont assign the object reference to the class
			user = new User();
			user.setErrorCode("404");
			user.setErrorMessage("Invalid Credentials...Please try again.");
			logger.debug("invalid credentials");
		} else {
			user.setIs_online('Y');
			userDAO.update(user);
			user.setErrorCode("200");
			user.setErrorMessage("You are successfully logged in....");
			session.setAttribute("loggedInUserID", user.getId());
			session.setAttribute("loggedInUserRole", user.getRole());
			logger.debug("You are logged in with the following role : " +session.getAttribute("loggedInUserRole"));
		}

		return new ResponseEntity<User>(user, HttpStatus.OK);

	}
	
	@RequestMapping(value = "/updateUser", method = RequestMethod.PUT)
	public ResponseEntity<User> updateUser(@RequestBody User user) {
		logger.debug("->->->->calling method updateUser");
		if (userDAO.getById(user.getId()) == null) {
			logger.debug("->->->->User does not exist with id " + user.getId());
			user = new User(); // ?
			user.setErrorCode("404");
			user.setErrorMessage("User does not exist with id " + user.getId());
			return new ResponseEntity<User>(user, HttpStatus.OK);
		}

		userDAO.update(user);
		logger.debug("->->->->User updated successfully");
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	@RequestMapping(value = "/accept/{id}", method = RequestMethod.GET)
	public ResponseEntity<User> accept(@PathVariable("id") String id) {
		logger.debug("Starting of the method accept");

		user = updateStatus(id, 'A', "");
		logger.debug("Ending of the method accept");
		return new ResponseEntity<User>(user, HttpStatus.OK);

	}
	@RequestMapping(value = "/reject/{id}/{reason}", method = RequestMethod.GET)
	public ResponseEntity<User> reject(@PathVariable("id") String id, @PathVariable("reason") String reason) {
		logger.debug("Starting of the method reject");

		user = updateStatus(id, 'R', reason);
		logger.debug("Ending of the method reject");
		return new ResponseEntity<User>(user, HttpStatus.OK);

	}
	@RequestMapping(value = "/myProfile", method = RequestMethod.GET)
	public ResponseEntity<User> myProfile(HttpSession session) {
		logger.debug("->->calling method myProfile");
		String loggedInUserID = (String) session.getAttribute("loggedInUserID");
		User user = userDAO.getById(loggedInUserID);
		if (user == null) {
			logger.debug("->->->-> User does not exist wiht id" + loggedInUserID);
			user = new User(); // It does not mean that we are inserting new row
			user.setErrorCode("404");
			user.setErrorMessage("User does not exist");
			return new ResponseEntity<User>(user, HttpStatus.NOT_FOUND);
		}
		logger.debug("->->->-> User exist with id" + loggedInUserID);
		logger.debug(user.getName());
		return new ResponseEntity<User>(user, HttpStatus.OK);
	}
	
	@RequestMapping(value="/listAllUsersNotFriends",method=RequestMethod.GET)
	public ResponseEntity<List<User>> listAllUsersNotFriends(HttpSession session) {
		logger.debug("->->->->->-> calling method listAllUsersNotFriends");
		String loggedInUserID=(String) session.getAttribute("loggedInUserID");
		List users = userDAO.notMyFriends(loggedInUserID);
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
	@GetMapping("/tLogout")
	public void logout()
	{
		String loggedInUserID=(String) session.getAttribute("loggedInUserID");
		user=userDAO.getById(loggedInUserID);
		user.setIs_online('N');
		userDAO.update(user);
		session.invalidate();
	
	}
	private User updateStatus(String id, char status, String reason) {
		logger.debug("Starting of the method updateStatus");

		logger.debug("status: " + status);
		user = userDAO.getById(id);

		if (user == null) {
			user = new User();
			user.setErrorCode("404");
			user.setErrorMessage("Could not update the status to " + status);
		} else {

			user.setStatus(status);
			user.setReason(reason);
			
			userDAO.update(user);
			
			user.setErrorCode("200");
			user.setErrorMessage("Updated the status successfully");
		}
		logger.debug("Ending of the method updateStatus");
		return user;

	}
	
}
