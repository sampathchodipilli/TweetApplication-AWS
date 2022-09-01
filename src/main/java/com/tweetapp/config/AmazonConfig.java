package com.tweetapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class AmazonConfig {
	
	@Bean
	public AmazonSNSClient amazonSNSClient() {
		return (AmazonSNSClient) AmazonSNSClientBuilder
				.standard()
				.withRegion("ap-south-1")
				.build();
	}
	
	@Bean
	public AmazonSQS amazonSQS() {
		return AmazonSQSClientBuilder.standard()
				.withRegion("ap-south-1")
				.build();
	}
}
