package am.ik.home;


import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.Map;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
@Getter
@ToString
public class MoneygrUser implements Serializable {
    private String userId;
    private String email;
    private String displayName;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void init() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        OAuth2Authentication auth = OAuth2Authentication.class.cast(authentication);
        Map<String, Object> details = (Map<String, Object>) auth.getUserAuthentication().getDetails();
        this.userId = details.get("id").toString();
        this.email = details.get("email").toString();
        Map<String, Object> name = (Map<String, Object>) details.get("name");
        this.displayName = name.get("familyName") + " " + name.get("givenName");
    }
}
