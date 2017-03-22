package com.niit.blogmiddleend.controller;

import java.util.Date;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.beans.factory.annotation.Autowired;
import com.niit.blogbackend.model.Message;
import com.niit.blogbackend.model.OutputMessage;
import com.niit.blogbackend.model.PrivateMessage;

@Controller
@RequestMapping("/")

public class ChatController {
	@Autowired
	private SimpMessagingTemplate simpMessagingTemplate;

	@RequestMapping(method = RequestMethod.GET)
	public String viewApplication() {
		return "index";
	}

	@MessageMapping("/chat")
	@SendTo("/topic/message")
	public OutputMessage sendMessage(Message message) {
		return new OutputMessage(message, new Date());
	}

	@MessageMapping("/privatechat")
	public PrivateMessage sendPmessage(PrivateMessage privatemessage) {
		privatemessage.setTime(new Date());
		simpMessagingTemplate.convertAndSend("/queue/message/" + privatemessage.getFriendID(), privatemessage);
		simpMessagingTemplate.convertAndSend("/queue/message/" + privatemessage.getName(), privatemessage);
		System.out.println("Date :::"+new Date());
		return new PrivateMessage(privatemessage,privatemessage.getTime() , privatemessage.getFriendID());
	}
}
