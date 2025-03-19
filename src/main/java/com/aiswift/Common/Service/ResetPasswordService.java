package com.aiswift.Common.Service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.aiswift.Common.Entity.BaseUser;
import com.aiswift.Exception.NoDataFoundException;
import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Repository.DeveloperRepository;
import com.aiswift.Global.Repository.OwnerRepository;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Repository.TenantUserRepository;

@Service
public class ResetPasswordService {
	@Autowired
	private TenantUserRepository userRepository;

	@Autowired
	private DeveloperRepository developerRepository;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private PasswordEncoder passwordEncode;

	private static final String DEVELOPER = "DEVELOPER";
	private static final String OWNER = "OWNER";
	private static final String USER = "USER";

	public void resetUserPassword(String token, String password, String shopName, String globalUser) {
		// globalUser: Owner, developer
		// shopname: shop 1 - tenant
		BaseUser user = null;

		if (globalUser != null) {
			user = findGlobalUserByResetToken(globalUser, token);
		}else if (shopName != null) {
			user = findTenantUserByResetToken(token);
		}else {
			throw new IllegalArgumentException("Missing required headers: Either 'global-user' or 'shop-name' must be provided.");
        }
		
		validateAndResetPassword(user, password);
	}

	private BaseUser findGlobalUserByResetToken(String globalUser, String token) {
		return switch (globalUser) {
		case DEVELOPER -> developerRepository.findByResetToken(token)
				.orElseThrow(() -> new RuntimeException("Developer not found" + token));
		case OWNER ->
			ownerRepository.findByResetToken(token).orElseThrow(() -> new RuntimeException("Owner not found" + token));
		default -> throw new IllegalArgumentException("Invalid global user type: " + globalUser);
		};
	}

	private BaseUser findTenantUserByResetToken(String token) {
		return userRepository.findByResetToken(token).orElseThrow(() -> new NoDataFoundException("User not found for this token: " + token));
	}

	private void validateAndResetPassword(BaseUser user, String password) {
		LocalDateTime expiredTime = user.getResetTokenExpiry();
		if (expiredTime.isBefore(LocalDateTime.now())) {
			throw new IllegalArgumentException("Token has expired.");
		}

		user.setPassword(passwordEncode.encode(password));
		user.setResetToken(null);
		user.setResetTokenExpiry(null);

		if (user instanceof Developer) {
			developerRepository.save((Developer) user);
		} else if (user instanceof Owner) {
			ownerRepository.save((Owner) user);
		} else if (user instanceof TenantUser) {
			userRepository.save((TenantUser) user);
		} else {
			throw new IllegalArgumentException("Unsupported user type.");
		}

	}
}
