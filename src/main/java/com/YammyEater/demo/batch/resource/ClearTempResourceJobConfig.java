package com.YammyEater.demo.batch.resource;


import com.YammyEater.demo.domain.upload.TempResource;
import com.YammyEater.demo.repository.upload.TempResourceRepository;
import com.YammyEater.demo.service.upload.TransactionResourceUploadService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.stream.Collectors;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ClearTempResourceJobConfig {

    @Value("${resource.temp-resource.effective-minute}")
    private long TEMP_RESOURCE_EFFECTIVE_MINUTE;
    @Value("${resource.temp-resource.batch.clear-job-name}")
    private String CLEAR_TEMP_RESOURCE_JOB_NAME;
    @Value("${resource.temp-resource.batch.clear-step-name}")
    private String CLEAR_TEMP_RESOURCE_STEP_NAME;
    @Value("${resource.temp-resource.batch.clear-chunk-size}")
    private int CHUNK_SIZE;


    private final TempResourceRepository tempResourceRepository;
    private final TransactionResourceUploadService transactionResourceUploadService;

    @Bean
    Job clearTempResourceJob(JobRepository jobRepository, Step clearTempResourceStep) {
        return new JobBuilder(CLEAR_TEMP_RESOURCE_JOB_NAME)
                .repository(jobRepository)
                .start(clearTempResourceStep)
                .build();
    }

    @Bean
    @JobScope
    Step clearTempResourceStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager,
            ItemReader<TempResource> clearTempResourceReader,
            ItemWriter<TempResource> clearTempResourceWriter
    ) {
        return new StepBuilder(CLEAR_TEMP_RESOURCE_STEP_NAME)
                .repository(jobRepository)
                .transactionManager(transactionManager)
                .<TempResource, TempResource>chunk(CHUNK_SIZE)
                .reader(clearTempResourceReader)
                .writer(clearTempResourceWriter)
                .build();
    }

    /*
     * 반환형을 ItemReader로 추상화하면 이에 대한 프록시를 생성하기 때문에 ItemStream의 자식이 아니게 되고
     * SimpleStepBuilder의 registerAsStreamsAndListeners에서 ItemStream의 하위 타입 검사에 실패해서
     * JpaPagingItemReader에서 재정의된 doOpen이 호출되지 않아 entityManager가 null이 되어 오류가 발생.
     */
    @Bean
    @StepScope
    JpaPagingItemReader<TempResource> clearTempResourceReader(
            JobRepository jobRepository,
            EntityManagerFactory entityManagerFactory,
            @Value("#{jobParameters['startTime']}") Date startTimeDate
    ) {
        LocalDateTime startTime = LocalDateTime.ofInstant(startTimeDate.toInstant(), ZoneId.systemDefault());
        //만료 시간 = 작업 시작 시간 - 유효 시간
        //만료 시간 이전에 업로드 된 모든 자원을 읽는다.
        LocalDateTime expireTime = startTime.minusMinutes(TEMP_RESOURCE_EFFECTIVE_MINUTE);

        //항상 첫번째 페이지를 읽는 reader로 재정의
        JpaPagingItemReader<TempResource> reader = new JpaPagingItemReader<>(){
            @Override
            public int getPage() {
                return 0;
            }
        };
        reader.setName("clearTempResourceReader");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setQueryString("SELECT tr FROM TempResource tr WHERE tr.uploadTime < :expireTime");
        reader.setParameterValues(Map.of("expireTime", expireTime));
        reader.setPageSize(CHUNK_SIZE);
        return reader;
    }

    @Bean
    @StepScope
    ItemWriter<TempResource> clearTempResourceWriter(JobRepository jobRepository) {
        return items -> {
            //모든 임시 자원을 트랜젝션 커밋 이후에 삭제하도록 설정
            transactionResourceUploadService.deleteResourcesAsyncAfterCommit(
                    items.stream().map(TempResource::getKey).collect(Collectors.toList())
            );
            //모든 임시 자원을 db에서 삭제
            tempResourceRepository.deleteAll(items);
        };
    }
}
