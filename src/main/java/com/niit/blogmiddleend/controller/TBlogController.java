package com.niit.blogmiddleend.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.niit.blogbackend.dao.BlogDAO;
import com.niit.blogbackend.model.Blog;

@RestController
public class TBlogController {
	private static final Logger logger = LoggerFactory.getLogger(TBlogController.class);

	@Autowired
	private BlogDAO blogDAO;

	@Autowired
	private Blog blog;

	@Autowired
	private HttpSession session;

	@GetMapping("/getblogs")
	public List<Blog> getBlogs() {
		logger.debug("calling method getBlogs");

		List blogs = blogDAO.getAllBlogs();

		if (blogs == null)

		{
			blog = new Blog();

			blog.setErrorCode("404");
			blog.setErrorMessage("No blogs are available");
			blogs.add(blog);

		}
		return blogs;

		// How it is returning JSONArray without proper return type i.e.,
		// ResponseEntity<List<Blog>>
	}

	@GetMapping("/blog/{id}")
	public Blog getBlog(@PathVariable("id") int id) {
		logger.debug("**************calling method getBlogs with the id " + id);
		Blog blog = blogDAO.getBlog(id);
		if (blog == null) {
			blog = new Blog();
			blog.setErrorCode("404");
			blog.setErrorMessage("Blog not found with the id:" + id);
		}

		return blog;
		/*
		 * if (blog == null) { return new
		 * ResponseEntity<Blog>(HttpStatus.NOT_FOUND); }
		 * 
		 * return new ResponseEntity<Blog>(blog, HttpStatus.OK);
		 */
	}

	@PostMapping(value = "/blog")
	public Blog createBlog(@RequestBody Blog blog, HttpSession session) {
		logger.debug("calling method createBlog with id " + blog.getId());

		String loggedInUserID = (String) session.getAttribute("loggedInUserID");
		logger.debug(" Blog is creating by the blog :" + loggedInUserID);
		if (loggedInUserID == null) {
			blog.setErrorCode("404");
			blog.setErrorMessage("You have to log in to create a blog");
			return blog;
		}
		blog.setDateTime(new Date());
		blog.setUserID(loggedInUserID);
		blog.setStatus("N");// A->Accepted, R->Rejected
		if (blogDAO.save(blog)) {
			blog.setErrorCode("200");
			blog.setErrorMessage("Successfully created the blog");
			return blog;
		} else {
			blog.setErrorCode("404");
			blog.setErrorMessage("Not able to create the blog");
			return blog;
		}

	}

	@PutMapping("/blog/{id}")
	public ResponseEntity<Blog> updateBlog(@PathVariable int id, @RequestBody Blog blog) {
		logger.debug("calling method updateBlog with the id " + id);

		if (blogDAO.getBlog(id) == null) {
			return new ResponseEntity<Blog>(HttpStatus.NOT_FOUND);
		}
		blogDAO.update(blog);
		return new ResponseEntity<Blog>(blog, HttpStatus.OK);
	}

	@PutMapping("/tApproveBlog/{blogID}")
	public Blog approveBlog(@PathVariable("blogID") Integer blogID)
	
	{
	   //get the blog based on blogID
		//If the blog does not exist -> cannot approve
		//If the blog is already approved -> can no tapprove again
		//set the statys as "A"
		//call update method
		
		//Check whether the user is logged or not
		
		//Check the user is admin or not
		
		if (session.getAttribute("loggedInUserID") == null)
		{
			blog.setErrorMessage("Please login to approve the blog");
			blog.setErrorCode("404");
			return blog;
		}
		
		if (! session.getAttribute("loggedInUserRole").equals("ROLE_ADMIN"))
		{
			blog.setErrorMessage("You are not authorized to approve the blog");
			blog.setErrorCode("404");
			return blog;
		}
		
		blog =  blogDAO.getBlog(blogID);
		
		if(blog==null)
		{
			blog = new Blog();
			blog.setErrorMessage("No blog exist with this id : " + blogID);
			blog.setErrorCode("404");
			return blog;
			
		}
		
		if(blog.getStatus().equals('A'))
		{
			blog.setErrorMessage("This blog is already approved : " + blogID);
			blog.setErrorCode("404");
			return blog;
		}
		
		
		blog.setStatus("A");
		
		if( blogDAO.update(blog))
		{
			blog.setErrorCode("200");
			blog.setErrorMessage("Successfully approved");
			
		}
		else
		{
			blog.setErrorCode("404");
			blog.setErrorMessage("Not able to approve the blog");
		}
		
		return blog;
		
}
}
