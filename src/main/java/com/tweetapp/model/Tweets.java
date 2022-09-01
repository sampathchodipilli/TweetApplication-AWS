package com.tweetapp.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "tweets")
public class Tweets {

	@Id
	private String id;

	@Field(name = "tweet_description")
	private String tweetDescription;

	private String email;

	private String username;

	private String firstName;

	private String lastName;

	@Field(name = "creation_time")
	private LocalDateTime creationTime;

	@Field(name = "like_count")
	private Integer likeCount;

	private List<Reply> replies;

	@Field(name = "liked_by_list")
	private List<String> likedByList;

	@Transient
	private String likedBy;

	public Tweets() {
		super();
	}

	public Tweets(String id, String tweetDescription, String email, String username, String firstName, String lastName,
			LocalDateTime creationTime, Integer likeCount, List<Reply> replies, List<String> likedByList,
			String likedBy) {
		super();
		this.id = id;
		this.tweetDescription = tweetDescription;
		this.email = email;
		this.username = username;
		this.firstName = firstName;
		this.lastName = lastName;
		this.creationTime = creationTime;
		this.likeCount = likeCount;
		this.replies = replies;
		this.likedByList = likedByList;
		this.likedBy = likedBy;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTweetDescription() {
		return tweetDescription;
	}

	public void setTweetDescription(String tweetDescription) {
		this.tweetDescription = tweetDescription;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public LocalDateTime getCreationTime() {
		return creationTime;
	}

	public void setCreationTime(LocalDateTime creationTime) {
		this.creationTime = creationTime;
	}

	public Integer getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(Integer likeCount) {
		this.likeCount = likeCount;
	}

	public List<Reply> getReplies() {
		return replies;
	}

	public void setReplies(List<Reply> replies) {
		this.replies = replies;
	}

	public List<String> getLikedByList() {
		return likedByList;
	}

	public void setLikedByList(List<String> likedByList) {
		this.likedByList = likedByList;
	}

	public String getLikedBy() {
		return likedBy;
	}

	public void setLikedBy(String likedBy) {
		this.likedBy = likedBy;
	}

	@Override
	public String toString() {
		return "Tweets [id=" + id + ", tweetDescription=" + tweetDescription + ", email=" + email + ", username="
				+ username + ", firstName=" + firstName + ", lastName=" + lastName + ", creationTime=" + creationTime
				+ ", likeCount=" + likeCount + ", replies=" + replies + ", likedByList=" + likedByList + ", likedBy="
				+ likedBy + "]";
	}

}
