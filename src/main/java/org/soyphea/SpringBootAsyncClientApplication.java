package org.soyphea;

import lombok.extern.slf4j.Slf4j;
import org.soyphea.domain.BaseResponse;
import org.soyphea.domain.BaseResultCallBack;
import org.soyphea.job.STUserTagFetchingService;
import org.soyphea.worker.MemoryCallbackStorageWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executor;

@SpringBootApplication
@EnableAsync
@RestController
@Slf4j
public class SpringBootAsyncClientApplication{

    @Autowired
    private STUserTagFetchingService stUserTagFetchingService;

    @Autowired
    private MemoryCallbackStorageWorker memoryCallbackStorageWorker;

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(100);
        executor.setMaxPoolSize(100);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("ST-Overflow-");
        executor.initialize();
        return executor;
    }

    public static void main(String[] args) {

        SpringApplication.run(SpringBootAsyncClientApplication.class, args);
    }

    @GetMapping("/api/stackoverflow/{user_id}/tags")
    BaseResponse create(@PathVariable("user_id") String userId) throws Exception {
        BaseResponse apiResponse = memoryCallbackStorageWorker.generateCallBackIdWithInitStatus();
        log.info("API Response with callback id:{}", apiResponse.getCallBackId());
        stUserTagFetchingService.execute(userId, apiResponse.getCallBackId());
        return apiResponse;
    }

    @GetMapping("/api/stackoverflow/{callback_id}/callbacks")
    BaseResultCallBack get(@PathVariable("callback_id") String callBackId) {
        return memoryCallbackStorageWorker.findResultFromStorage(callBackId).get();
    }


}
