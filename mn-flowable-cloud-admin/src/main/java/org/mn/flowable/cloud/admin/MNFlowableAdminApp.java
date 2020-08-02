package org.mn.flowable.cloud.admin;


import org.mn.flowable.cloud.common.RestInterceptor;
import org.mn.flowable.cloud.common.rest.idm.remote.RemoteAccountResource;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.mn.flowable.cloud.common.shiro.ShiroConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication(
        scanBasePackageClasses = {
                MNFlowableAdminApp.class,
                RemoteAccountResource.class,
                ShiroConfig.class,
                RemoteIdmService.class
        }
)
@EnableDiscoveryClient
@EnableFeignClients(
        basePackages = "org.mn.flowable.cloud.admin.feignclients",
        basePackageClasses = {
                //RemoteIdmServiceClient.class
        }
)
public class MNFlowableAdminApp {
    public static void main(String[] args) {
        SpringApplication.run(MNFlowableAdminApp.class);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestInterceptor()));
        return restTemplate;
    }
}
