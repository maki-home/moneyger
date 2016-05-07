package am.ik.home;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.hateoas.hal.Jackson2HalModule;
import org.springframework.http.RequestEntity;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@SpringBootApplication
@EnableOAuth2Sso
public class MoneygrApplication extends WebSecurityConfigurerAdapter {
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.antMatcher("/**").authorizeRequests()
                .antMatchers("/").permitAll()
                .anyRequest().authenticated();
    }

    @Controller
    static class MoneygrController {
        @Autowired
        OAuth2RestTemplate restTemplate;
        @Autowired
        MoneygrUser user;
        @Value("${inout.uri:http://localhost:7777/api}")
        URI inoutUri;

        @RequestMapping("/")
        String index() {
            return "index";
        }

        @RequestMapping("/home")
        String home(Model model) {
            Resources<Outcome> outcomes = restTemplate.exchange(
                    RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri).pathSegment("outcomes").build().toUri()).build(),
                    new ParameterizedTypeReference<Resources<Outcome>>() {
                    }
            ).getBody();
            model.addAttribute("outcomes", outcomes);
            model.addAttribute("user", user);
            return "home";
        }
    }

    @Bean
    InitializingBean restTemplateInitializer(OAuth2RestTemplate restTemplate) {
        return () -> restTemplate.getMessageConverters().stream()
                .filter(c -> c instanceof MappingJackson2HttpMessageConverter)
                .findAny()
                .ifPresent(c -> {
                    MappingJackson2HttpMessageConverter converter = (MappingJackson2HttpMessageConverter) c;
                    ObjectMapper objectMapper = converter.getObjectMapper();
                    objectMapper.registerModule(new Jackson2HalModule());
                });
    }

    @Bean
    JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    public static void main(String[] args) {
        SpringApplication.run(MoneygrApplication.class, args);
    }
}
