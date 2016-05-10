package am.ik.home;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Serializable;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@Getter
@ToString
public class MoneygrUser implements Serializable {
    private String userId;
    private String email;
    private String displayName;

    @Autowired
    ObjectMapper objectMapper;

    @PostConstruct
    public void init() throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2Authentication auth = OAuth2Authentication.class.cast(authentication);
        OAuth2AuthenticationDetails details = OAuth2AuthenticationDetails.class.cast(auth.getDetails());
        String payload = details.getTokenValue().split("\\.")[1];
        JsonNode json = objectMapper.readValue(Base64Utils.decodeFromUrlSafeString(payload), JsonNode.class);
        this.userId = json.get("user_id").asText();
        this.email = json.get("user_name").asText();
        this.displayName = json.get("display_name").asText();
    }
}
