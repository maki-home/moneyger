package am.ik.home;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionCache {

    private Map<String, Map<Integer, String>> categories = Collections.emptyMap();
    private Map<String, String> members = Collections.emptyMap();

    @Autowired
    OAuth2RestTemplate restTemplate;
    @Value("${inout.uri}")
    URI inoutUri;
    @Value("${member.uri}")
    URI memberUri;

    @PostConstruct
    public void init() {
        this.categories = Collections.unmodifiableMap(categories());
        this.members = Collections.unmodifiableMap(members());
    }

    public Map<String, Map<Integer, String>> getCategories() {
        return categories;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    private Map<String, Map<Integer, String>> categories() {
        JsonNode categories = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomeCategories")
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, Map<Integer, String>> cat = new LinkedHashMap<>();
        for (JsonNode node : categories.get("_embedded").get("outcomeCategories")) {
            String key = node.get("parentOutcomeCategory").get("parentCategoryName").asText();
            cat.computeIfAbsent(key, x -> new LinkedHashMap<>());
            cat.get(key).put(node.get("categoryId").asInt(), node.get("categoryName").asText());
        }
        return cat;
    }

    private Map<String, String> members() {
        JsonNode members = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(memberUri)
                        .pathSegment("members")
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, String> memberMap = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(members.get("_embedded").get("members").elements(), Spliterator.ORDERED), false)
                .collect(toMap(
                        node -> node.get("memberId").asText(),
                        node -> node.get("familyName").asText() + " " + node.get("givenName").asText()
                ));
        return memberMap;
    }
}
