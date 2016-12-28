package am.ik.home;

import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.*;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;

import am.ik.home.client.member.Member;
import am.ik.home.client.member.MemberClient;

@Component
@SessionScope
public class SessionCache {

	@Autowired
	OAuth2RestTemplate restTemplate;
	@Autowired
	MemberClient memberClient;
	@Value("${inout.uri}")
	URI inoutUri;
	private Map<String, Map<Integer, String>> outcomeCategories = Collections.emptyMap();
	private Map<Integer, String> incomeCategories = Collections.emptyMap();
	private Map<String, String> members = Collections.emptyMap();

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
		JsonNode categories = restTemplate
				.exchange(RequestEntity
						.get(UriComponentsBuilder.fromUri(inoutUri)
								.pathSegment("outcomeCategories").build().toUri())
						.build(), JsonNode.class)
				.getBody();
		Map<String, Map<Integer, String>> cat = new LinkedHashMap<>();
		for (JsonNode node : categories.get("_embedded").get("outcomeCategories")) {
			String key = node.get("parentOutcomeCategory").get("parentCategoryName")
					.asText();
			cat.computeIfAbsent(key, x -> new LinkedHashMap<>());
			cat.get(key).put(node.get("categoryId").asInt(),
					node.get("categoryName").asText());
		}
		return cat;
	}

	private Map<Integer, String> incomeCategories() {
		JsonNode categories = restTemplate.exchange(
				RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
						.pathSegment("incomeCategories").build().toUri()).build(),
				JsonNode.class).getBody();
		Map<Integer, String> cat = StreamSupport
				.stream(Spliterators.spliteratorUnknownSize(
						categories.get("_embedded").get("incomeCategories").elements(),
						Spliterator.ORDERED), false)
				.collect(toMap(node -> node.get("categoryId").asInt(),
						node -> node.get("categoryName").asText()));
		return cat;
	}

	private Map<String, String> members() {
		return memberClient.findAll().getContent().stream().collect(toMap(
				Member::getMemberId, m -> m.getFamilyName() + " " + m.getGivenName()));
	}
}
