package com.hrm.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.hrm.common.Constants;
import com.hrm.entity.Employee;
import com.hrm.entity.Profile;
import com.hrm.exception.RecordNotFoundException;
import com.hrm.model.request.LoginRequest;
import com.hrm.model.response.LoginResponse;
import com.hrm.repository.EmployeeRepository;
import com.hrm.repository.ProfileRepository;
import com.hrm.security.UserDetailsImpl;
import com.hrm.security.UserDetailsServiceImpl;
import com.hrm.security.jwt.JwtTokenUtils;
import com.hrm.utils.Utils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

	@Autowired
	JwtTokenUtils jwtTokenUtil;

	@Autowired
	UserDetailsServiceImpl userDetailsServiceImpl;

	@Autowired
	EmployeeRepository employeeRepository;

	@Autowired
	ProfileRepository profileRepository;

	@Autowired
	private JwtTokenUtils jwtTokenUtils;
	
	@Value("${oauth.client.id}")
	private String clientId;

	@Value("${oauth.client.secret}")
	private String clientSecret;

	public LoginResponse login(LoginRequest request) throws GeneralSecurityException, IOException {
		GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
				// Specify the CLIENT_ID of the app that accesses the backend:
				.setAudience(Arrays.asList(clientId))
				// Or, if multiple clients access the backend:
				// .setAudience(Arrays.asList(CLIENT_ID_1, CLIENT_ID_2, CLIENT_ID_3))
				.build();

		// (Receive idTokenString by HTTPS POST)
		if (org.apache.commons.lang3.StringUtils.isNotBlank(request.getToken())) {
			GoogleIdToken idToken = verifier.verify(request.getToken());
			if (idToken != null) {
				Payload payload = idToken.getPayload();

				// Print user identifier
				String userId = payload.getSubject();
				System.out.println("User ID: " + userId);

				// Get profile information from payload
				String email = payload.getEmail();
				boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());
				Optional<Employee> opEmployee = employeeRepository.findByEmailAndDeleteFlag(email.trim(),
						Constants.DELETE_NONE);
				if (opEmployee.isEmpty()) {
					throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
				}
				Optional<Profile> opProfile = profileRepository.getByEmployeeId(opEmployee.get().getId());
				if (opEmployee.isEmpty()) {
					throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
				}
				if (emailVerified == true) {
					String jwt = jwtTokenUtils.generateJwtToken(request.getEmail());
					List<String> roles = UserDetailsImpl.build(opEmployee.get()).getAuthorities().stream()
							.map(GrantedAuthority::getAuthority).collect(Collectors.toList());
					return new LoginResponse(opEmployee.get().getId(), opProfile.get().getFullName(),
							opEmployee.get().getEmail(), jwt, jwtTokenUtils.getExtAt(jwt).getTime(), roles);
				}
			} else {
				throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
			}
		}
		Optional<Employee> opEmployee = employeeRepository.findByEmailAndDeleteFlag(request.getEmail().trim(),
				Constants.DELETE_NONE);
		if (opEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		Optional<Profile> opProfile = profileRepository.getByEmployeeId(opEmployee.get().getId());
		if (opEmployee.isEmpty()) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}
		String password = Utils.decrypt(opEmployee.get().getPassword(), Constants.SECRET);
		if (password == null || !password.equals(request.getPassword())) {
			throw new RecordNotFoundException(Constants.RECORD_NOT_FOUND);
		}

		String jwt = jwtTokenUtils.generateJwtToken(request.getEmail());
		List<String> roles = UserDetailsImpl.build(opEmployee.get()).getAuthorities().stream()
				.map(GrantedAuthority::getAuthority).collect(Collectors.toList());
		return new LoginResponse(opEmployee.get().getId(), opProfile.get().getFullName(), opEmployee.get().getEmail(),
				jwt, jwtTokenUtils.getExtAt(jwt).getTime(), roles);
	}

	public Boolean authWithToken(String token) {
		if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
			token = token.substring(7);
			String email = jwtTokenUtil.getEmailFromJwtToken(token);
			UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);
			return userDetails != null;
		}
		return false;
	}
}
