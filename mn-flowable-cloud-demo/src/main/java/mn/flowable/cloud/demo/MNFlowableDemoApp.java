package mn.flowable.cloud.demo;

import org.mn.flowable.cloud.common.feign.RemoteIdmServiceClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "mn.flowable.cloud.demo.feignclients"
},
        basePackageClasses = {
                RemoteIdmServiceClient.class
        })
public class MNFlowableDemoApp {
    public static void main(String[] args) {
        SpringApplication.run(MNFlowableDemoApp.class);
    }

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
