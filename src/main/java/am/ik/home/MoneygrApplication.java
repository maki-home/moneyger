package am.ik.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import org.apache.catalina.filters.RequestDumperFilter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.boot.autoconfigure.web.HttpMessageConverters;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignFormatterRegistrar;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.format.datetime.standard.DateTimeFormatterRegistrar;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;


@SpringBootApplication
@EnableOAuth2Sso
@EnableZuulProxy
@EnableFeignClients
public class MoneygrApplication extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated();
    }

    @Profile("!cloud")
    @Bean
    RequestDumperFilter requestDumperFilter() {
        return new RequestDumperFilter();
    }

    @Bean
    InitializingBean messageConvertersInitializer(HttpMessageConverters messageConverters) {
        return () -> messageConverters.getConverters().stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .ifPresent(c -> {
                    MappingJackson2HttpMessageConverter converter = (MappingJackson2HttpMessageConverter) c;
                    ObjectMapper objectMapper = converter.getObjectMapper();
                    objectMapper.registerModule(new Jackson2HalModule());
                });
    }

    @Bean
    RequestInterceptor oauth2FeignRequestInterceptor(OAuth2ClientContext oauth2ClientContext, OAuth2ProtectedResourceDetails resource) {
        return new OAuth2FeignRequestInterceptor(oauth2ClientContext, resource);
    }

    @Bean
    FeignFormatterRegistrar localDateFeignFormatterRegistrar() {
        return formatterRegistry -> {
            DateTimeFormatterRegistrar registrar = new DateTimeFormatterRegistrar();
            registrar.setUseIsoFormat(true);
            registrar.registerFormatters(formatterRegistry);
        };
    }

    public static void main(String[] args) {
        SpringApplication.run(MoneygrApplication.class, args);
    }
}
