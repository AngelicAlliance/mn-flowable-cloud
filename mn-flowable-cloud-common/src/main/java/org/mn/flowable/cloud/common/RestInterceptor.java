package org.mn.flowable.cloud.common;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Component
public class RestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
            throws IOException {
        HttpHeaders headers = request.getHeaders();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest oldrequest = attributes.getRequest();

        Cookie[] cookies = oldrequest.getCookies();
        String token = null;
        for (Cookie cookie : cookies) {
            if ("X-Access-Token".equals(cookie.getName())) {
                token = cookie.getValue();
            }
        }
        //添加token
        headers.add("X-Access-Token", token);

        // 保证请求继续被执行
        return execution.execute(request, body);
    }

}