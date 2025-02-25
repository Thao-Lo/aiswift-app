package com.aiswift.Config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.aiswift.MultiTenancy.TenantInterceptor;
@Configuration
public class WebConfig implements WebMvcConfigurer{
	private final TenantInterceptor tenantInterceptor;
	
	public WebConfig(TenantInterceptor tenantInterceptor) {
		this.tenantInterceptor = tenantInterceptor;
	}
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(tenantInterceptor)
		.addPathPatterns("/api/**");
	}
	

}
