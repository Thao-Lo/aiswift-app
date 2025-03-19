package com.aiswift.Global.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Repository.DeveloperRepository;


@Service
public class DeveloperService {

	@Autowired
	DeveloperRepository developerRepository;
	
	public Developer getDeveloperByEmail(String email) {		
		return developerRepository.findByEmail(email)
				.orElseThrow(() -> new NoDataFoundException ("Developer not found for this email: " + email));
				
	}
	public Developer getByResetToken(String token) {
		return developerRepository.findByResetToken(token)
				.orElseThrow(() -> new NoDataFoundException ("Developer not found for this token: " + token));
	}
}
