package com.YammyEater.demo.batch;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.YammyEater.demo.AwsS3MockConfig;
import com.YammyEater.demo.domain.upload.TempResource;
import com.YammyEater.demo.repository.upload.TempResourceRepository;
import com.amazonaws.services.s3.AmazonS3;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@SpringBatchTest
@SpringBootTest
@Import(AwsS3MockConfig.class)
@ActiveProfiles("test")
class ClearTempResourceJobConfigTest {

    /*
     * 여러 방법을 생각하다가 이것으로 최종 결정했다.
     * Job이 1개 이상 빈으로 등록된 상황에서 JobLauncherTestUtils에 주입하기 위해 테스트 대상 job을 primary로 정의한다.
     * 직접 컴포넌트 스캔과 자동 설정을 지정해 주는 설정 클래스를 만드는 방법도 있지만 부작용이 많다.
     */
    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        @Primary
        Job testJob(@Qualifier("clearTempResourceJob") Job clearTempResourceJob) {
            return clearTempResourceJob;
        }
    }

    @Autowired
    private TempResourceRepository tempResourceRepository;

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    AmazonS3 amazonS3;

    @Test
    public void doNothing() {}

    @BeforeEach
    public void init() {
        tempResourceRepository.deleteAll();
    }
    @Test
    public void success() throws Exception {
        //given
        //유효기간이 지난 자원 20개 생성
        int expiredResourceCount = 20;
        LocalDateTime resourceUploadTime = LocalDateTime.of(2020, 10, 25, 0, 0, 0);
        for(int i=0;i<expiredResourceCount;i++) {
            tempResourceRepository.save(
                    TempResource.builder()
                            .key(String.valueOf(i))
                            .uploadTime(resourceUploadTime)
                            .build()
            );
        }

        //when
        //유효기간이 지난 자원 삭제
        Random random = new Random();
        LocalDateTime expireLocalDateTime = resourceUploadTime.plusHours(1L);
        Date expireDate = Date.from(expireLocalDateTime.atZone(ZoneId.systemDefault()).toInstant());
        JobParameters jobParameters = new JobParametersBuilder()
                .addDate("startTime", expireDate)
                .addLong("unique", random.nextLong())
                .toJobParameters();

        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        //then
        //s3에 20번 삭제가 요청되고 db에서 tempResource가 삭제되었는지 확인
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);
        verify(amazonS3, Mockito.times(expiredResourceCount)).deleteObject(any(), any());
        List<TempResource> tempResourceList = tempResourceRepository.findAll();
        assertThat(tempResourceList.size()).isEqualTo(0);
    }
}