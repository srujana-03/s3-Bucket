package com.rest.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


@Configuration
public class S3Config {
	
	@Value("${aws.accessKey}")
	private String awsAcessKey;
	
	@Value("${aws.secretKey}")
	private String awsSecretKey;
	
	@Value("${aws.region}")
	private String awsRegion;
	
	@Value("${aws.s3.bucketName}")
    private String awsS3BucketName;
	
	@Bean
	public S3Client s3Client() {
		AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(awsAcessKey, awsSecretKey);
				return S3Client.builder()
		                .region(Region.of(awsRegion))  
		                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))  
		                .build();
	}
	
	@Bean
    public String getBucketName() {
        return awsS3BucketName;
    }
	
	

}
