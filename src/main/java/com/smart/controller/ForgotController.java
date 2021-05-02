package com.smart.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.service.EmailService;

@Controller
public class ForgotController {
	
	Random random = new Random(1000);
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// Email Id form open Handler
	@RequestMapping("/forgot")
	public String openEmailForm()
	{
		
		return "forgot_email_form";
	}
	
	// Send OTP Handler
		@PostMapping("/send-otp")
		public String sendOTP(@RequestParam("email") String email,HttpSession session)
		{
			System.out.println("Email "+email);
			
			// Generating 4 digit OTP
			int otp = random.nextInt(99999);
			System.out.println("OTP : "+otp);
			
			// Code to send OTP to email
			
			String subject = "OTP From SCM";
			String message = ""
							+"<div class='card' style='border:1px solid #e2e2e2; padding:20px'>"
							+"<div class='card-body'>"
							+"<h5 class='card-title'>"
							+"OTP is : "
							+"<b>"+otp
							+"</h5>"
							+"</div>"
							+"</div>";
			String to = email;
			
			boolean flag = this.emailService.sendEmail(subject, message, to);
			
			if(flag)
			{
				session.setAttribute("sessionotp",otp);
				session.setAttribute("email", email);
				return "verify_otp";
			}else 
			{
				session.setAttribute("message", "Check your Email Id");
				return "forgot_email_form";
			}
			
		}
		
		// Verify otp 
		@PostMapping("/verify-otp")
		public String verifyOtp(@RequestParam("otp") int otp,HttpSession session)
		{
			
			int sessionotp = (int)session.getAttribute("sessionotp");
			String email = (String)session.getAttribute("email");
			if(sessionotp==otp)
			{
				// Password Change form
				
				User user = this.userRepository.getUserByUserName(email);
				
				if(user==null)
				{
					// Send Error Message
					
					session.setAttribute("message", "User does not exist with this email");
					return "forgot_email_form";
					
				}else
				{
					// Send Change Password form
					
				}
				
				return "password_change_form";
			}else
			{
				
				session.setAttribute("message", "You have entered Wrong OTP");
				return "verify_otp";
			}
		}
		
		// Change Password Handler
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("newpassword") String newpassword,HttpSession session)
		{
			String email = (String)session.getAttribute("email");
			User user = this.userRepository.getUserByUserName(email);
			user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
			this.userRepository.save(user);
			
			return "redirect:/signin?change=password changed successfully....";
		}
	
}
