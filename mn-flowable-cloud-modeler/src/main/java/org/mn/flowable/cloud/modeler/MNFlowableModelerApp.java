package org.mn.flowable.cloud.modeler;

import org.apache.ibatis.annotations.Mapper;
import org.mn.flowable.cloud.common.RestInterceptor;
import org.mn.flowable.cloud.common.properties.FlowableCommonAppProperties;
import org.mn.flowable.cloud.common.rest.idm.remote.RemoteAccountResource;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmService;
import org.mn.flowable.cloud.common.shiro.ShiroConfig;
import org.mn.flowable.cloud.common.util.DefaultTenantProvider;
import org.mn.flowable.cloud.common.util.UuidIdGenerator;
import org.mn.flowable.cloud.modeler.properties.FlowableModelerAppProperties;
import org.mn.flowable.cloud.modeler.servlet.ApiDispatcherServletConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.util.Collections;


@EnableConfigurationProperties({
        FlowableCommonAppProperties.class,
        FlowableModelerAppProperties.class
})
@SpringBootApplication(
        proxyBeanMethods = false
)
@ComponentScan(
        basePackageClasses = {
                MNFlowableModelerApp.class,
                DefaultTenantProvider.class,
                UuidIdGenerator.class,
                RemoteAccountResource.class,
                ShiroConfig.class,
                RemoteIdmService.class
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {
                        ApiDispatcherServletConfiguration.class
                })
        }
)
@EnableFeignClients(basePackageClasses = {
        //RemoteIdmServiceClient.class,
})
@MapperScan(basePackages = "org.mn.flowable.cloud.modeler.mapper",
        annotationClass = Mapper.class)
@EnableDiscoveryClient
public class MNFlowableModelerApp {
    public static void main(String[] args) {
        SpringApplication.run(MNFlowableModelerApp.class, args);
    }

    @Bean
    public ServletRegistrationBean modelerApiServlet(ApplicationContext applicationContext) {
        AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        dispatcherServletConfiguration.setParent(applicationContext);
        dispatcherServletConfiguration.register(ApiDispatcherServletConfiguration.class);
        DispatcherServlet servlet = new DispatcherServlet(dispatcherServletConfiguration);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet, "/api/*");
        registrationBean.setName("Flowable Modeler App API Servlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new RestInterceptor()));
        return restTemplate;
    }
}
