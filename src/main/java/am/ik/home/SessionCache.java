package am.ik.home;

import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.*;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import am.ik.home.client.member.Member;
import am.ik.home.client.member.MemberClient;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionCache {

    private Map<String, Map<Integer, String>> outcomeCategories = Collections.emptyMap();
    private Map<Integer, String> incomeCategories = Collections.emptyMap();
    private Map<String, String> members = Collections.emptyMap();

    @Autowired
    OAuth2RestTemplate restTemplate;
    @Autowired
    MemberClient memberClient;
    @Value("${inout.uri}")
    URI inoutUri;

    @PostConstruct
    public void init() {
        this.outcomeCategories = Collections.unmodifiableMap(outcomeCategories());
        this.incomeCategories = Collections.unmodifiableMap(incomeCategories());
        this.members = Collections.unmodifiableMap(members());
    }

    public Map<String, Map<Integer, String>> getOutcomeCategories() {
        return outcomeCategories;
    }

    public Map<Integer, String> getIncomeCategories() {
        return incomeCategories;
    }

    public Map<String, String> getMembers() {
        return members;
    }

    private Map<String, Map<Integer, String>> outcomeCategories() {
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

    private Map<Integer, String> incomeCategories() {
        JsonNode categories = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("incomeCategories")
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<Integer, String> cat = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(categories.get("_embedded").get("incomeCategories").elements(), Spliterator.ORDERED), false)
                .collect(toMap(node -> node.get("categoryId").asInt(), node -> node.get("categoryName").asText()));
        return cat;
    }

	private Map<String, String> members() {
		return memberClient.findAll().getContent().stream().collect(toMap(
				Member::getMemberId, m -> m.getFamilyName() + " " + m.getGivenName()));
	}
}
