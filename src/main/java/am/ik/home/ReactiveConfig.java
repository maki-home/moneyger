package am.ik.home;

import static org.springframework.web.reactive.function.client.ClientRequest.from;

import javax.net.ssl.SSLException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.codec.StringDecoder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientStrategies;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

@Configuration
public class ReactiveConfig {

	@Bean
	WebClient webClient(ObjectMapper objectMapper) throws SSLException {
		SslContext sslContext = SslContextBuilder.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		WebClientStrategies strategies = WebClientStrategies.empty()
				.decoder(new Jackson2JsonDecoder(objectMapper))
				.decoder(new StringDecoder()).build();
		return WebClient
				.builder(
						new ReactorClientHttpConnector(opt -> opt.sslContext(sslContext)))
				.strategies(strategies).filter(authorization()).build();
	}

	ExchangeFilterFunction authorization() {
		return (clientRequest, exchangeFunction) -> exchangeFunction
				.exchange(from(clientRequest)
						.header("Authorization", "Bearer " + accessToken()).build());
	}

	String accessToken() {
		Authentication authentication = SecurityContextHolder.getContext()
				.getAuthentication();
		OAuth2Authentication auth = OAuth2Authentication.class.cast(authentication);
		OAuth2AuthenticationDetails details = OAuth2AuthenticationDetails.class
				.cast(auth.getDetails());
		return details.getTokenValue();
	}
}
