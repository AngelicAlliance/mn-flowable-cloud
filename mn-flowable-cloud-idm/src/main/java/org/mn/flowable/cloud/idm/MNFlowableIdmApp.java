package org.mn.flowable.cloud.idm;


import org.mn.flowable.cloud.common.properties.FlowableCommonAppProperties;
import org.mn.flowable.cloud.common.service.idm.RemoteIdmServiceImpl;
import org.mn.flowable.cloud.idm.properties.FlowableIdmAppProperties;
import org.mn.flowable.cloud.idm.servlet.ApiDispatcherServletConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;


@EnableConfigurationProperties({
        FlowableIdmAppProperties.class,
        FlowableCommonAppProperties.class
})
@ComponentScan(
        basePackages = {
                "org.mn.flowable.cloud.idm",
                "org.mn.flowable.common.rest.exception"
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {
                        RemoteIdmServiceImpl.class,
                        ApiDispatcherServletConfiguration.class
                })
        })
@SpringBootApplication(proxyBeanMethods = false)
@EnableAsync
public class MNFlowableIdmApp {
    public static void main(String[] args) {
        SpringApplication.run(MNFlowableIdmApp.class, args);
    }

    @Bean
    public ServletRegistrationBean apiServlet(ApplicationContext applicationContext) {
        AnnotationConfigWebApplicationContext dispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        dispatcherServletConfiguration.setParent(applicationContext);
        dispatcherServletConfiguration.register(ApiDispatcherServletConfiguration.class);
        DispatcherServlet servlet = new DispatcherServlet(dispatcherServletConfiguration);
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(servlet, "/api/*");
        registrationBean.setName("Flowable IDM App API Servlet");
        registrationBean.setLoadOnStartup(1);
        registrationBean.setAsyncSupported(true);
        return registrationBean;
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
