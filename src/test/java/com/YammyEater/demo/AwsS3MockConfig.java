package com.YammyEater.demo;

import static org.mockito.Mockito.mock;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.context.annotation.Bean;

/*
 * Configuration으로 정의되는 Bean은 @MockBean으로 대체가 불가능하므로 @Import나 @TestConfiguration을 이용
 */
public class AwsS3MockConfig {
        @Bean
        public AmazonS3 amazonS3() {
            return mock(AmazonS3.class);
        }
}
