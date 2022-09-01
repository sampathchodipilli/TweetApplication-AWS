package com.tweetapp.service;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.tweetapp.constants.Constants;
import com.tweetapp.dto.ForgotPasswordDto;
import com.tweetapp.exception.EmailAlreadyExistsException;
import com.tweetapp.exception.UsernameAlreadyExistsException;
import com.tweetapp.model.AuthRequest;
import com.tweetapp.model.AuthResponse;
import com.tweetapp.model.Response;
import com.tweetapp.model.User;
import com.tweetapp.repository.UserRepository;


@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private JwtTokenService jwtTokenService;
	
	@Value("${tweet.app.sns.arn}")
	private String snsArn;
	
	@Autowired
	private AmazonSNSClient amazonSNSClient;
	
	private Logger logger = LogManager.getLogger(UserService.class);

	public AuthResponse login(AuthRequest authRequest) {
		logger.info("Inside login() ...");
		AuthResponse authResponse;
		Optional<User> optional = userRepository.findByEmailAndPassword(authRequest.getUsername(), authRequest.getPassword());
		if (optional.isPresent()) {
			String acccessToken = jwtTokenService.generateAcccessToken(authRequest.getUsername());
			User user = optional.get();
			user.setPassword("********");
			authResponse = new AuthResponse(acccessToken, user);
			
			String message = user.getFirstName()+" "+user.getLastName()+" Logged into Tweet-Application";
			
			publishMessage(message, snsArn);
		} else {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unable to login, Check your credentials !");
		}
		return authResponse;
	}

	private void publishMessage( String message, String snsArn2) {
		try {
			logger.info("SNS -- start publishing");
			logger.info("Topic Arn :: "+snsArn2);
			
			PublishRequest publishRequest = new PublishRequest(snsArn2, message);
			
			PublishResult publish = amazonSNSClient.publish(publishRequest);
			
			logger.info("SNS Message Id :: "+publish.getMessageId());
			logger.info("SNS -- Message published");
		} catch(Exception e) {
			logger.error("Error :: "+e);
		}
	}

	public User registerUser(User user) throws Exception {
		logger.info("Inside registerUser() ...");
		User savedUser;
		Optional<User> findByEmail = userRepository.findByEmail(user.getEmail());
		Optional<User> findByUsername = userRepository.findByUsername(user.getUsername());
		if (findByEmail.isPresent() && findByUsername.isPresent()) {
			throw new Exception("The Entered Email and username is already taken !");
		} else if (findByUsername.isPresent()) {
			throw new UsernameAlreadyExistsException("The Entered Username is already taken !");
		} else if (findByEmail.isPresent()) {
			throw new EmailAlreadyExistsException("The Entered Email is already taken");
		} else {
			try {
				savedUser = userRepository.save(user);
				savedUser.setPassword("********");
			} catch(Exception e) {
				logger.error("Error : ",e);
				savedUser = null;
			}
		}
		return savedUser;
	}

	public Response forgotPassword(String email, ForgotPasswordDto forgotPasswordDto) {
		logger.info("Inside forgotPassword() ...");
		Response response;
		Optional<User> optional = userRepository.findByEmail(email);
		if(optional.isPresent()) {
			User user = optional.get();
			user.setPassword(forgotPasswordDto.getPassword());
			userRepository.save(user);
			logger.info("User password updated !");
			response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Password changed successfully");
		} else {
			response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "Unable to change password, please check the username");
		}
		return response;
	}

	public Response getAllUsers() {
		logger.info("Inside getAllUsers() ...");
		Response response;
		List<User> findAll = userRepository.findAll();
		try {
			if (!ObjectUtils.isEmpty(findAll) && findAll.size()>0) {
				findAll.stream().forEach(rec -> {
					rec.setPassword("********");
				});
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "users found", findAll);
			} else {
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "No users exist !", null);
			}
		} catch (Exception e) {
			response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No users Found !", null);
		}
		return response;
	}

	public Response getUserByUsername(String email) {
		logger.info("Inside getUserByUsername() ...");
		Response response;
		Optional<User> optional = userRepository.findByEmail(email);
		if (optional.isPresent()) {
			User user = optional.get();
			user.setPassword("********");
			response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "User Found", user);
		} else {
			response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "User Not Found", null);
		}
		return response;
	}

}
