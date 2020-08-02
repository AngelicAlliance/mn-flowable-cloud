package org.mn.flowable.cloud.common.shiro;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: Scott
 * @date: 2018/2/7
 * @description: shiro 配置类
 */

@Slf4j
@Configuration
public class ShiroConfig {

	@Bean
	public static LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
		return new LifecycleBeanPostProcessor();
	}

	/**
	 * Filter Chain定义说明
	 * <p>
	 * 1、一个URL可以配置多个Filter，使用逗号分隔
	 * 2、当设置多个过滤器时，全部验证通过，才视为通过
	 * 3、部分过滤器可指定参数，如perms，roles
	 */
	@Bean("shiroFilter")
	public ShiroFilterFactoryBean shiroFilter(SecurityManager securityManager) {
		ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
		shiroFilterFactoryBean.setSecurityManager(securityManager);
		// 拦截器
		Map<String, String> filterChainDefinitionMap = new LinkedHashMap<String, String>();

		//cas验证登录
		filterChainDefinitionMap.put("/cas/client/validateLogin", "anon");
		// 配置不会被拦截的链接 顺序判断
		filterChainDefinitionMap.put("/sys/randomImage/**", "anon"); //登录验证码接口排除
		filterChainDefinitionMap.put("/sys/checkCaptcha", "anon"); //登录验证码接口排除
		filterChainDefinitionMap.put("/sys/login", "anon"); //登录接口排除
		filterChainDefinitionMap.put("/sys/mLogin", "anon"); //登录接口排除
		filterChainDefinitionMap.put("/sys/logout", "anon"); //登出接口排除
		filterChainDefinitionMap.put("/thirdLogin/**", "anon"); //第三方登录
		filterChainDefinitionMap.put("/sys/getEncryptedString", "anon"); //获取加密串
		filterChainDefinitionMap.put("/sys/sms", "anon");//短信验证码
		filterChainDefinitionMap.put("/sys/phoneLogin", "anon");//手机登录
		filterChainDefinitionMap.put("/sys/user/checkOnlyUser", "anon");//校验用户是否存在
		filterChainDefinitionMap.put("/sys/user/register", "anon");//用户注册
		filterChainDefinitionMap.put("/sys/user/querySysUser", "anon");//根据手机号获取用户信息
		filterChainDefinitionMap.put("/sys/user/phoneVerification", "anon");//用户忘记密码验证手机号
		filterChainDefinitionMap.put("/sys/user/passwordChange", "anon");//用户更改密码
		filterChainDefinitionMap.put("/auth/2step-code", "anon");//登录验证码
		filterChainDefinitionMap.put("/sys/common/static/**", "anon");//图片预览 &下载文件不限制token
		filterChainDefinitionMap.put("/sys/common/pdf/**", "anon");//pdf预览
		filterChainDefinitionMap.put("/generic/**", "anon");//pdf预览需要文件
		filterChainDefinitionMap.put("/", "anon");
		filterChainDefinitionMap.put("/doc.html", "anon");
		filterChainDefinitionMap.put("/**/*.js", "anon");
		filterChainDefinitionMap.put("/**/*.css", "anon");
		filterChainDefinitionMap.put("/**/*.html", "anon");
		filterChainDefinitionMap.put("/**/*.svg", "anon");
		filterChainDefinitionMap.put("/**/*.pdf", "anon");
		filterChainDefinitionMap.put("/**/*.jpg", "anon");
		filterChainDefinitionMap.put("/**/*.png", "anon");
		filterChainDefinitionMap.put("/**/*.ico", "anon");

		// update-begin--Author:sunjianlei Date:20190813 for：排除字体格式的后缀
		filterChainDefinitionMap.put("/**/*.ttf", "anon");
		filterChainDefinitionMap.put("/**/*.woff", "anon");
		filterChainDefinitionMap.put("/**/*.woff2", "anon");
		// update-begin--Author:sunjianlei Date:20190813 for：排除字体格式的后缀

		filterChainDefinitionMap.put("/druid/**", "anon");
		filterChainDefinitionMap.put("/swagger-ui.html", "anon");
		filterChainDefinitionMap.put("/swagger**/**", "anon");
		filterChainDefinitionMap.put("/webjars/**", "anon");
		filterChainDefinitionMap.put("/v2/**", "anon");

		//性能监控
		filterChainDefinitionMap.put("/actuator/metrics/**", "anon");
		filterChainDefinitionMap.put("/actuator/httptrace/**", "anon");
		filterChainDefinitionMap.put("/actuator/redis/**", "anon");

		filterChainDefinitionMap.put("/actuator/**", "anon");

		//大屏设计器排除
		filterChainDefinitionMap.put("/big/screen/**", "anon");

		//测试示例
		filterChainDefinitionMap.put("/test/jeecgDemo/html", "anon"); //模板页面
		filterChainDefinitionMap.put("/test/jeecgDemo/redis/**", "anon"); //redis测试

		//websocket排除
		filterChainDefinitionMap.put("/websocket/**", "anon");

		// 添加自己的过滤器并且取名为jwt
		Map<String, Filter> filterMap = new HashMap<String, Filter>(1);
		filterMap.put("jwt", new JwtFilter());
		shiroFilterFactoryBean.setFilters(filterMap);
		// <!-- 过滤链定义，从上向下顺序执行，一般将/**放在最为下边
		filterChainDefinitionMap.put("/**", "jwt");

		// 未授权界面返回JSON
		shiroFilterFactoryBean.setUnauthorizedUrl("/sys/common/403");
		shiroFilterFactoryBean.setLoginUrl("/sys/common/403");
		shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
		return shiroFilterFactoryBean;
	}

	@Bean("securityManager")
	public DefaultWebSecurityManager securityManager(ShiroRealm myRealm) {
		DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
		securityManager.setRealm(myRealm);

		/*
		 * 关闭shiro自带的session，详情见文档
		 * http://shiro.apache.org/session-management.html#SessionManagement-
		 * StatelessApplications%28Sessionless%29
		 */
		DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
		DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
		defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
		subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
		securityManager.setSubjectDAO(subjectDAO);
		//自定义缓存实现,使用redis
		//securityManager.setCacheManager(redisCacheManager());
		return securityManager;
	}

	/**
	 * 下面的代码是添加注解支持
	 *
	 * @return
	 */
	@Bean
	@DependsOn("lifecycleBeanPostProcessor")
	public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
		DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
		defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
		/**
		 * 解决重复代理问题 github#994
		 * 添加前缀判断 不匹配 任何Advisor
		 */
		defaultAdvisorAutoProxyCreator.setUsePrefix(true);
		defaultAdvisorAutoProxyCreator.setAdvisorBeanNamePrefix("_no_advisor");
		return defaultAdvisorAutoProxyCreator;
	}

	@Bean
	public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
		AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
		advisor.setSecurityManager(securityManager);
		return advisor;
	}

}
