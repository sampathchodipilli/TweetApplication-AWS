package com.tweetapp.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.tweetapp.constants.Constants;
import com.tweetapp.model.Reply;
import com.tweetapp.model.Response;
import com.tweetapp.model.Tweets;
import com.tweetapp.model.User;
import com.tweetapp.repository.TweetRepository;
import com.tweetapp.repository.UserRepository;

@Service
public class TweetService {

	@Autowired
	private TweetRepository tweetRepository;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private KafkaProducerService kafkaProducerService;
	
	@Value("${tweet.app.sqs}")
	private String QUEUE_NAME;
	
	@Autowired
	private AmazonSQS sqs;

	private Logger logger = LogManager.getLogger(TweetService.class);

	public ResponseEntity<Response> getAllTweets() {
		logger.info("Inside getAllTweets() ...");
		Response response;
		try {
			List<Tweets> list = tweetRepository.findAll();
			if (!ObjectUtils.isEmpty(list)) {
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, null, list);
			} else {
				response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No tweets Found", null);
			}
		} catch (Exception e) {
			logger.error("Error : ", e);
			response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No tweets Found", null);
		}
		return new ResponseEntity<Response>(response, HttpStatus.OK);
	}

	public Response getTweetsByUsername(String email) {
		logger.info("Inside getTweetsByUsername() ...");
		Response response;
		try {
			Optional<List<Tweets>> optional = tweetRepository.findByEmail(email);
			Optional<User> userOptional = userRepository.findByEmail(email);
			if (optional.isPresent() && optional.get().size() > 0) {
				List<Tweets> list = optional.get();
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Tweets Found", optional.get());
			} else {
				response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No Tweets Found", null);
			}
		} catch (Exception e) {
			logger.error("Error : ", e);
			response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No Tweets Found", null);
		}
		return response;
	}

	public Response postNewTweet(String email, Tweets tweet) {
		logger.info("Inside postNewTweet() ...");
		Response response;
		Optional<User> optional = userRepository.findByEmail(email);
		if (optional.isPresent()) {
			User user = optional.get();
			tweet.setUsername(user.getUsername());
			tweet.setFirstName(user.getFirstName());
			tweet.setLastName(user.getLastName());
		} else {
			throw new UsernameNotFoundException("User Not Found !");
		}
		tweet.setEmail(email);
		tweet.setCreationTime(LocalDateTime.now());
		tweet.setLikeCount(0);
		tweet.setLikedByList(new ArrayList<>());
		tweet.setReplies(new ArrayList<>());
		try {
			Tweets savedTweet = tweetRepository.save(tweet);
			response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Tweet Saved");
		} catch (Exception e) {
			logger.error("Error : ", e);
			return new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "Unable to save tweet");
		}
		return response;
	}

	public Response updateTweet(String email, String id, Tweets tweet) {
		logger.info("Inside updateTweet() ...");
		Response response;
		Optional<Tweets> optional = tweetRepository.findById(id);
		try {
			if (optional.isPresent()) {
				Tweets tweets = optional.get();
				tweets.setTweetDescription(tweet.getTweetDescription());
				tweets.setCreationTime(LocalDateTime.now());
				tweetRepository.save(tweets);
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Tweet Updated");
			} else {
				response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "Tweet Updation Failed");
			}
		} catch (Exception e) {
			logger.error("Error : ", e);
			response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "Tweet Updation Failed");
		}
		return response;
	}

	public Response deleteTweet(String email, String id) {
		Response response;
		logger.info("Inside deleteTweet()...");
		logger.info("sqs name :: "+QUEUE_NAME);
		String queueUrl = sqs.getQueueUrl(QUEUE_NAME).getQueueUrl();
		SendMessageRequest messageRequest = new SendMessageRequest().withQueueUrl(queueUrl).withMessageBody(id).withDelaySeconds(0);
		sqs.sendMessage(messageRequest);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		kafkaProducerService.sendMessaged(id);
		response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Tweet Deleted");
		return response;
	}

	public Response likeTweet(String email, String id) {
		logger.info("Inside likeTweet() ...");
		Response response;
		Optional<Tweets> optional = tweetRepository.findById(id);
		if (optional.isPresent()) {
			Tweets tweet = optional.get();
			List<String> likedByList = tweet.getLikedByList();
			if (!checkEmailInList(email, likedByList)) {
				tweet.setLikeCount(tweet.getLikeCount() + 1);
				likedByList.add(email);
				tweet.setLikedByList(likedByList);
				tweetRepository.save(tweet);
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Tweet Liked");
			} else {
				response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "User Already liked the tweet");
			}

		} else {
			response = new Response(Constants.FAILED, Constants.INTERNAL_ERROR, "No tweet with this id exists");
		}
		return response;
	}

	private boolean checkEmailInList(String email, List<String> likedByList) {
		Optional<String> findAny = likedByList.stream().filter(rec -> rec.equals(email)).findAny();
		if (findAny.isPresent()) {
			return true;
		}
		return false;
	}

	public Response replyToTweet(String email, String id, Reply reply) {
		logger.info("Inside replyToTweet() ...");
		Response response;
		reply.setCreationTime(LocalDateTime.now());
		reply.setEmail(email);
		try {
			Optional<Tweets> optional = tweetRepository.findById(id);
			Optional<User> optional2 = userRepository.findByEmail(email);
			if (optional.isPresent() && optional2.isPresent()) {
				Tweets tweet = optional.get();
				User user = optional2.get();
				reply.setUsername(user.getUsername());
				reply.setFirstName(user.getFirstName());
				reply.setLastName(user.getLastName());
				List<Reply> replies = tweet.getReplies();
				replies.add(reply);
				tweet.setReplies(replies);
				Tweets savedTweet = tweetRepository.save(tweet);
				response = new Response(Constants.SUCCESS, Constants.HTTP_OK, "Reply Added");
			} else {
				response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "Unable to reply");
			}
		} catch (Exception e) {
			logger.error("Error : ", e);
			response = new Response(Constants.FAILED, Constants.BAD_REQUEST, "Unable to reply");
		}
		return response;
	}

}
