package com.niit.blogmiddleend.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.niit.blogbackend.dao.FriendDAO;
import com.niit.blogbackend.dao.UserDAO;
import com.niit.blogbackend.model.Friend;
import com.niit.blogbackend.model.User;

@RestController
public class TFriendController {
	private static final Logger logger = LoggerFactory.getLogger(TFriendController.class);
	@Autowired
	UserDAO userDAO;
	@Autowired
	User user;
	@Autowired
	HttpSession httpSession;
	@Autowired
	FriendDAO friendDAO;
	@Autowired
	Friend friend;
	
	@RequestMapping(value = "/myFriends", method = RequestMethod.GET)
	public ResponseEntity<List<Friend>> getMyFriends() {
		logger.debug("->->->->calling method getMyFriends");
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		List<Friend> myFriends = new ArrayList<Friend>();
		if(loggedInUserID == null)
		{
			friend.setErrorCode("404");
			friend.setErrorMessage("Please login to know your friends");
			myFriends.add(friend);
			return new ResponseEntity<List<Friend>>(myFriends, HttpStatus.OK);
			
		}
       
		logger.debug("getting friends of : " + loggedInUserID);
		 myFriends = friendDAO.getMyFriends(loggedInUserID);

		if (myFriends.isEmpty()) {
			logger.debug("Friends does not exist for the user : " + loggedInUserID);
			friend.setErrorCode("404");
			friend.setErrorMessage("You do not have any friends");
			myFriends.add(friend);
		}
		logger.debug("Send the friend list ");
		return new ResponseEntity<List<Friend>>(myFriends, HttpStatus.OK);
	}

	@RequestMapping(value = "/addFriend/{friendID}", method = RequestMethod.PUT)
	public ResponseEntity<Friend> sendFriendRequest(@PathVariable("friendID") String friendID) {
		logger.debug("->->->->calling method sendFriendRequest");
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		friend.setUserID(loggedInUserID);
		friend.setFriendID(friendID);
		friend.setStatus("N"); // N - New, R->Rejected, A->Accepted
		friend.setIsOnline('N');
		// Is the user already sent the request previous?
		

		

		if (friendDAO.get(loggedInUserID, friendID) != null) {
			friend.setErrorCode("404");
			friend.setErrorMessage("You already sent the friend request to " + friendID);

		} else {
			friendDAO.addFriend(friend);

			friend.setErrorCode("200");
			friend.setErrorMessage("Friend request successfull.." + friendID);
		}

		return new ResponseEntity<Friend>(friend, HttpStatus.OK);

	}
	
	
	

	
	
	private boolean isFriendRequestAvailabe(String friendID)
	{
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		
		if(friendDAO.get(loggedInUserID,friendID)==null)
			return false;
		else
			return true;
	}

	@RequestMapping(value = "/unFriend/{userID}", method = RequestMethod.PUT)
	public ResponseEntity<Friend> unFriend(@PathVariable("userID") String userID) {
		logger.debug("->->->->calling method unFriend"+userID);
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		logger.debug("->->->->calling method unFriend"+loggedInUserID);
		friendDAO.unFriend(loggedInUserID,userID);
		//updateRequest(friendID, "U");
		logger.debug("->->->->setting status as U");
		logger.debug("->->->->Deleting the record");
		return new ResponseEntity<Friend>(friend, HttpStatus.OK);

	}

	@RequestMapping(value = "/rejectFriend/{userID}", method = RequestMethod.PUT)
	public ResponseEntity<Friend> rejectFriendFriendRequest(@PathVariable("userID") String userID) {
		logger.debug("->->->->calling method rejectFriendFriendRequest");

		updateRequest(userID, "R");
		return new ResponseEntity<Friend>(friend, HttpStatus.OK);

	}

	@RequestMapping(value = "/accepttFriend/{userID}", method = RequestMethod.PUT)
	public ResponseEntity<Friend> acceptFriendFriendRequest(@PathVariable("userID") String userID) {
		logger.debug("->->->->calling method acceptFriendFriendRequest");
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
        friend=friendDAO.get(userID,loggedInUserID );
		if(friend!=null)
		{
			friend.setStatus("A");
			friendDAO.save(friend);
		}
		return new ResponseEntity<Friend>(friend, HttpStatus.OK);

	}

	private Friend updateRequest(String friendID, String status) {
		logger.debug("Starting of the method updateRequest");
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		logger.debug("loggedInUserID : " + loggedInUserID);
		
		if(isFriendRequestAvailabe(friendID)==false)
		{
			friend.setErrorCode("404");
			friend.setErrorMessage("The request does not exist.  So you can not update to "+status);
		}
		
		if (status.equals("R")|| status.equals("U"))
			friend = friendDAO.get(friendID, loggedInUserID);
		else
			friend = friendDAO.get(loggedInUserID, friendID);
		friend.setStatus(status); // N - New, R->Rejected, A->Accepted

		friendDAO.update(friend);

		friend.setErrorCode("200");
		friend.setErrorMessage(
				"Request from   " + friend.getUserID() + " has been accepted by " + friend.getFriendID() + " and status has been updated to :" + status);
		logger.debug("Ending of the method updateRequest");
		return friend;

	}

	@RequestMapping(value = "/getMyFriendRequests/", method = RequestMethod.GET)
	public ResponseEntity<List<Friend>> getMyFriendRequests() {
		logger.debug("->->->->calling method getMyFriendRequests");
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		List<Friend> myFriendRequests = friendDAO.getNewFriendRequests(loggedInUserID);
		return new ResponseEntity<List<Friend>>(myFriendRequests, HttpStatus.OK);

	}
	
	
	
	@RequestMapping("/getRequestsSendByMe")
	public ResponseEntity<List<Friend>>  getRequestsSendByMe()
	{
		logger.debug("->->->->calling method getRequestsSendByMe");
		
		String loggedInUserID = (String) httpSession.getAttribute("loggedInUserID");
		List<Friend> requestSendByMe = friendDAO.getRequestsSendByMe(loggedInUserID);
		
		logger.debug("->->->->Ending method getRequestsSendByMe");
		
		return new ResponseEntity<List<Friend>>(requestSendByMe, HttpStatus.OK);
		
	}
}