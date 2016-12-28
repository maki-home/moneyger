package am.ik.home;

import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignFormatterRegistrar;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.web.context.annotation.SessionScope;

import com.fasterxml.jackson.databind.ObjectMapper;

import am.ik.home.client.member.MemberClient;
import am.ik.home.client.member.MemberClientImpl;
import am.ik.home.client.user.UaaUser;
import feign.RequestInterceptor;

@SpringBootApplication
@EnableOAuth2Sso
@EnableZuulProxy
@EnableFeignClients
@EnableBinding(MoneygrSource.class)
@IntegrationComponentScan
public class MoneygrApplication extends WebSecurityConfigurerAdapter {
	public static void main(String[] args) {
		SpringApplication.run(MoneygrApplication.class, args);
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.antMatcher("/**").authorizeRequests().antMatchers("/").permitAll()
				.anyRequest().authenticated();
	}

	@Profile("!cloud")
	@Bean
	RequestDumperFilter requestDumperFilter() {
		return new RequestDumperFilter();
	}

	@Bean
	@SessionScope
	UaaUser uaaUser(ObjectMapper objectMapper) {
		return new UaaUser(objectMapper);
	}

	@Bean
	MemberClient memberClient(@Value("${auth-server}") String apiBase,
			OAuth2RestTemplate oAuth2RestTemplate) {
		return new MemberClientImpl(apiBase, oAuth2RestTemplate);
	}

	@Bean
	Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
		return builder -> builder.modulesToInstall(new Jackson2HalModule());
	}

	@Bean
	RequestInterceptor oauth2FeignRequestInterceptor(
			OAuth2ClientContext oauth2ClientContext,
			OAuth2ProtectedResourceDetails resource) {
		return new OAuth2FeignRequestInterceptor(oauth2ClientContext, resource);
	}

	@Bean
	OAuth2RestTemplate oAuth2RestTemplate(OAuth2ProtectedResourceDetails resource,
			OAuth2ClientContext context, HttpMessageConverters messageConverters) {
		OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resource, context);
		restTemplate.setMessageConverters(messageConverters.getConverters());
		return restTemplate;
	}

	@Bean
	FeignFormatterRegistrar localDateFeignFormatterRegistrar() {
		return formatterRegistry -> {
			DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
			registrar.setUseIsoFormat(true);
			registrar.registerFormatters(formatterRegistry);
		};
	}
}
