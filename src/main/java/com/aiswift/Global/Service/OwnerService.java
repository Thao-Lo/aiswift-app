package com.aiswift.Global.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
	
	public Owner getByResetToken(String token) {
		return ownerRepository.findByResetToken(token)
				.orElseThrow(() -> new RuntimeException ("Owner not found for this toke: " + token));
	}
	
	public void saveOwner(Owner owner) {
		ownerRepository.save(owner);
	}
	
	// fetch SubPlanDetail List this time to avoid Lazy.Fetch in Entity
	@Transactional(transactionManager = "globalTransactionManager")
	    public Owner getOwnerWithSubPlanDetails(String email) {
	        Owner owner =getOwnerByEmail(email);	        
	        owner.getSubPlanDetails().size();	        
	        return owner;
	    }
}