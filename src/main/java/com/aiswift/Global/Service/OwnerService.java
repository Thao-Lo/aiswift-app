package com.aiswift.Global.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Repository.OwnerRepository;


@Service
public class OwnerService {

	@Autowired
	private OwnerRepository ownerRepository;
	
	public Owner getOwnerByEmail(String email) {
		return ownerRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("Owner not found: " + email));
	}

	public List<Owner> findAll() {
		// TODO Auto-generated method stub
		return ownerRepository.findAllWithTenants();
	}
}