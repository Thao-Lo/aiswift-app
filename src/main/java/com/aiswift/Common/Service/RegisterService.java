package com.aiswift.Common.Service;

import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import com.aiswift.Common.DTO.RegisterRequest;
import com.aiswift.Common.Entity.BaseUser;
import com.aiswift.Global.Entity.Developer;
import com.aiswift.Global.Entity.Owner;
import com.aiswift.Global.Repository.DeveloperRepository;
import com.aiswift.Global.Repository.OwnerRepository;
import com.aiswift.Tenant.Entity.TenantUser;
import com.aiswift.Tenant.Repository.TenantUserRepository;

import jakarta.mail.MessagingException;

@Service
public class RegisterService {
	@Autowired
	private TenantUserRepository userRepository;

	@Autowired
	private DeveloperRepository developerRepository;

	@Autowired
	private OwnerRepository ownerRepository;

	@Autowired
	private MultiEmailService multiEmailService;
	
	@Autowired
	private AuthService authService;

	private static final String DEVELOPER = "DEVELOPER";
	private static final String OWNER = "OWNER";
	private static final String USER = "USER";

	public void registerNewUser(RegisterRequest request, Principal principal, String shopName) {
		String userType = request.getUserType().toUpperCase();
		String email = request.getEmail();
		String registryRole = authService.getCurrentUserRole(principal);

		if (!Set.of(DEVELOPER, OWNER, USER).contains(userType)) {
			throw new IllegalArgumentException("Invalid User Type: " + request.getUserType());
		}
		boolean isGlobalRegistration = (shopName == null);
		boolean isTenantRegistration = (shopName != null);
		
		if(isGlobalRegistration) {
			if("SUPER_ADMIN".equals(registryRole) && !Set.of(DEVELOPER, OWNER).contains(userType)) {
				 throw new IllegalArgumentException("Super Admin can only register Developer or Owner.");
			}
			if("ADMIN".equals(registryRole) && !Set.of(OWNER).contains(userType)){
				throw new IllegalArgumentException("Admin (Developer) can only register Owner.");
			}
		}
		if(isTenantRegistration) {
			if(!USER.equals(userType)) {
				throw new IllegalArgumentException("Only 'USER' type can be registered in tenant");
			}
		}		
		
		//globalUser: DEVELOPER or OWNER, or else null
		String globalUser = userType.equalsIgnoreCase(USER) ? null : userType;
		//if shopName == null -> "DEVELOPER or OWNER", else shopName: shop1, 2,3
		String shopNameOrGlobalUser = (shopName == null) ? globalUser : shopName;
		
		//check registered email in db
		boolean isEmailExist = switch (userType) {
		case DEVELOPER -> developerRepository.existsByEmail(email);
		case OWNER -> ownerRepository.existsByEmail(email);
		case USER -> userRepository.existsByEmail(email);
		default -> false;
		};
		
		if (isEmailExist) {
			throw new IllegalArgumentException("Email is already existed " + email);
		}
		boolean isDeveloper = (Set.of(DEVELOPER, OWNER).contains(userType));

		switch (userType) {
		case DEVELOPER:
			createUser(Developer.class, request, developerRepository, principal.getName(), isDeveloper,
					shopNameOrGlobalUser);
			break;
		case OWNER:
			createUser(Owner.class, request, ownerRepository, principal.getName(), isDeveloper, shopNameOrGlobalUser);
			break;

		case USER:
			createUser(TenantUser.class, request, userRepository, principal.getName(), isDeveloper, shopNameOrGlobalUser);
			break;
		}
	}

	public <T extends BaseUser> T createUser(Class<T> userType, RegisterRequest request,
			JpaRepository<T, Long> repository, String registryEmail, boolean isDeveloper, String shopNameOrGlobalUser) {

		try {
			T user = userType.getDeclaredConstructor().newInstance(); // Reflection
			// Owner owner = new Owner();
			user.setFirstName(request.getFirstName());
			user.setLastName(request.getLastName());
			user.setEmail(request.getEmail());
			user.setCreatedBy(registryEmail);
			user.setResetToken(UUID.randomUUID().toString());
			user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // 1 hour

			T savedUser = repository.save(user);
			sendPasswordSetUpEmail(user.getEmail(), user.getResetToken(), isDeveloper, shopNameOrGlobalUser);
			return savedUser;
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException("Error creating user instance via Reflection: " + e.getMessage(), e);
		} catch (MessagingException e) {
			throw new RuntimeException("Error when registering new user. " + e.getMessage(), e);
		}
	}

	private void sendPasswordSetUpEmail(String email, String token, boolean isDeveloper, String shopNameOrGlobalUser)
			throws MessagingException {
		String subject = "Reset Password";

		String body = "<h3>Set Your New Password</h3>" + "</br>"
				+ "<p>Please send the following API request to reset your password:</p>" + "<pre>"
				+ "POST api/reset-password\n" + "Headers:\n"
				+ (isDeveloper ? "- global-user: " + shopNameOrGlobalUser + "\n"
						: "- shop-name: " + shopNameOrGlobalUser + "\n")
				+ "Content-Type: application/json\n\n" + "Body:\n" + "{\n" + "  \"token\": \"" + token + "\",\n"
				+ "  \"newPassword\": \"your-new-password\"\n" + "}" + "</pre>"
				+ "<p><b>Note:</b> The token is valid for 1 hour.</p>";

		multiEmailService.sendEmail(email, subject, body, isDeveloper);
	}
}
