package com.smart.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smart.entities.Contact;
import com.smart.entities.User;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
	
	// pagination
	
	// Page Interface --> A page is a sublist of a list of objects. It allows gain information about the 
	// 						position of it in the containing entire list.
	
	@Query("from Contact as c where c.user.id =:userId")
	public Page<Contact> findContactsByUser(@Param ("userId") int userId, Pageable pageable);
	
	public List<Contact> findByNameContainingAndUser(String name,User user);
	

}
