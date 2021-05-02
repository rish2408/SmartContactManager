package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ContactRepository contactRepository;
	
//	Method for adding common data to response
	
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		
//		Principal is used to fetch username using getName()
		
		String userName = principal.getName();
		System.out.println(userName);
		
//		Get the user using username(Email)
		
		User userByUserName = this.userRepository.getUserByUserName(userName);
		System.out.println("User : " +userByUserName);
		
		model.addAttribute("user",userByUserName);
		
	}
	
//	User Dashboard handler
	
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	
//	Open Add Contact form Handler
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		
		return "normal/add_contact_form";
	}
	
//	Processing Add Contact Form
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,
								@RequestParam("profileImage") MultipartFile file,
								Principal principal,
								HttpSession session) {
		
		try {
			
			String name = principal.getName();
			User userByUserName = this.userRepository.getUserByUserName(name);
			
//			Processing and Uploading File
			
			if (file.isEmpty()) {
				
				System.out.println("File is Empty");
				contact.setImage("default.png");
				
			} else {
				
//				Upload the file to folder and update the name to contact
				
				contact.setImage(file.getOriginalFilename());
				
				File saveFile = new ClassPathResource("static/img").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is Uploaded Successfully");
				
			}
			
			contact.setUser(userByUserName);
			userByUserName.getContacts().add(contact);
			
			this.userRepository.save(userByUserName);
			
			System.out.println("Contact Data : " +contact);
			
			System.out.println("Added to Database");
			
//			Success message Alert
			
			session.setAttribute("message", new Message("Your Contact is added Successfully !! Add More", "alert-success"));
			
			
		} catch (Exception e) {
			e.printStackTrace();
			
//			Error message Alert
			
			session.setAttribute("message", new Message("Oops Something went wrong !! Please Try Again", "alert-danger"));
			
		}
		
		return "normal/add_contact_form";
	}
	
//	Show Contacts Handler
	
	// Per Page 	= 5 Contacts = 5[n]
	// Current Page = 0 Page
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model model,Principal principal) {
		
//		Send Contact List From Here
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		
		Pageable pageable = PageRequest.of(page, 5);
		
		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(),pageable);
		
		model.addAttribute("contacts",contacts);
		model.addAttribute("currentPage",page);
		model.addAttribute("totalPages",contacts.getTotalPages());
		
		model.addAttribute("title","Show User Contacts");
		return "normal/show_contacts";
	}
	
	// Showing Particular Contact Details
	
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal) {
		
		System.out.println(cid);
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User userByUserName = this.userRepository.getUserByUserName(userName);
		
		if (userByUserName.getId()==contact.getUser().getId()) {
			model.addAttribute("contact",contact);
			model.addAttribute("title",contact.getName());
		}

		return "normal/contact_detail";
	}
	
	// Delete Contact Handler
	
	@GetMapping("/delete/{cid}")
	public String deleteContact(@PathVariable("cid") Integer cid,Model model,Principal principal,HttpSession session) {
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cid);
		Contact contact = contactOptional.get();
		
		User user = this.userRepository.getUserByUserName(principal.getName());
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
		System.out.println("Contact Deleted");
		session.setAttribute("message", new Message("Contact Deleted Successfully..","alert-success"));
		
		return "redirect:/user/show-contacts/0";
	}
	
	// Open Update Form Handler
	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model model) {
		
		Optional<Contact> optionalContact = this.contactRepository.findById(cid);
		Contact contact = optionalContact.get();
		
		model.addAttribute("contact",contact);
		model.addAttribute("title","Update Contact");
		return "normal/update_form";
	}
	
	// Update Contact Handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,Model model, HttpSession session,Principal principal) {
		
		try {
			
			// Old Contact Details
			Contact oldContactDetail = this.contactRepository.findById(contact.getCid()).get();
			
			// Image Operation
			if (!file.isEmpty()) {
				// Rewrite the file in database and delete old file
				
				// Delete Old file code
				
				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetail.getImage());
				file1.delete();
				
				// Update New File Code
				
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path , StandardCopyOption.REPLACE_EXISTING);
				
				contact.setImage(file.getOriginalFilename());
				System.out.println("Image is Uploaded Successfully");
				
			}else {
				contact.setImage(oldContactDetail.getImage());
			}
			
//			also add user id with updated contact
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			
//			Now Update contact
			this.contactRepository.save(contact);
			
			session.setAttribute("message", new Message("Your Contact is updated Successfully","alert-success"));
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		System.out.println("Contact Name : "+contact.getName());
		System.out.println("Contact Id : "+contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
//			Your Profile Handler
	
		@GetMapping("/profile")
		public String yourProfile(Model model) {
			
			model.addAttribute("title","Profile Page");
			return "normal/profile";
		}
		
// 			Open Setting Handler
		
		@GetMapping("/settings")
		public String openSettings(Model model)
		{
			model.addAttribute("title","Settings Page");
			return "normal/settings";
		}
		
//		Change Password Handler
		@PostMapping("/change-password")
		public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword,Principal principal,HttpSession httpSession)
		{
			System.out.println("OLD Password : "+oldPassword);
			System.out.println("NEW Password : "+newPassword);
			
			String userName = principal.getName();
			User currentUser = this.userRepository.getUserByUserName(userName);
			System.out.println(currentUser.getPassword());
			
			if(this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword()))
			{
				// Change Password
				currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
				this.userRepository.save(currentUser);
				httpSession.setAttribute("message", new Message("Password Changed Successfully","alert-success"));
			}else {
				// Error
				
				httpSession.setAttribute("message", new Message("Wrong Old Password","alert-danger"));
				return "redirect:/user/settings";
			}
			
			return "redirect:/user/index";
		}
}
