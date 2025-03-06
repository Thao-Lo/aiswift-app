package com.aiswift.dto;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.aiswift.Enum.Status;

import lombok.Data;

@Data
public class CustomUserDetails implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	private String email;
    private String password;
    private List<GrantedAuthority> authorities = new ArrayList<>();
    private String dbName;
    private Status status;


    public CustomUserDetails(String email,String password,Status status,List<GrantedAuthority> authorities,String dbName) {
    	this.email = email;
    	this.password = password;
    	this.status = status;
    	this.authorities = authorities;
    	this.dbName = dbName;
    	
    }
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
//		if(isGlobalUser) { 
//			authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));//OWNER or DEVELOPER, if its owner add ADMIN  as ROLE
//		}else {
//	        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); //ADMIN or STAFF, get from entity's role
//
//		}
		return authorities;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return password;
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return email;
	}
	@Override
    public boolean isAccountNonExpired() {
        return true; // Or based on business logic
    }
 
 @Override
    public boolean isAccountNonLocked() {
        return status == Status.ACTIVE;
    }
 @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
  @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE;
    }
 


}
