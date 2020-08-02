package org.mn.flowable.cloud.task;


import org.mn.flowable.cloud.common.RestInterceptor;
import org.mn.flowable.cloud.common.properties.FlowableCommonAppProperties;
import org.mn.flowable.cloud.common.rest.idm.remote.RemoteAccountResource;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.mn.flowable.cloud.common.shiro.ShiroConfig;
import org.mn.flowable.cloud.task.properties.FlowableTaskAppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@EnableConfigurationProperties({
        FlowableCommonAppProperties.class,
        FlowableTaskAppProperties.class
})
@SpringBootApplication(scanBasePackageClasses = {
        MNFlowableTaskApp.class,
        RemoteAccountResource.class,
        ShiroConfig.class,
        RemoteIdmService.class
})
@EnableFeignClients(basePackageClasses = {
        //RemoteIdmServiceClient.class,
})
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
public class MNFlowableTaskApp {

    public static void main(String[] args) {
        SpringApplication.run(MNFlowableTaskApp.class, args);
    }

    @Bean
    public WebMvcConfigurer workflow() {
        return new WebMvcConfigurer() {

            @Override
            public void addViewControllers(@NonNull ViewControllerRegistry registry) {
                registry.addViewController("/workflow").setViewName("redirect:/workflow/");
                registry.addViewController("/workflow/").setViewName("forward:/workflow/index.html");
            }
        };
    }

    @Bean
    public TaskExecutorCustomizer flowableTaskTaskExecutorCustomizer() {
        return taskExecutor -> {
            // Not yet exposed via properties in Spring Boot
            taskExecutor.setAwaitTerminationSeconds(30);
            taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        };
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestInterceptor()));
        return restTemplate;
    }


}
