package com.aiswift.Global.Service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Repository.DeveloperRepository;

@Service
public class DeveloperService {

	@Autowired
	DeveloperRepository developerRepository;
	
	public Optional<Developer> getDeveloperByEmail(String email) {
		// TODO Auto-generated method stub
		return developerRepository.findByEmail(email);
	}

}
