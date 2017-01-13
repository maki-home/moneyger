package am.ik.home;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import am.ik.home.client.member.Member;
import am.ik.home.client.member.MemberClient;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

@Component
@SessionScope
public class SessionCache {

	@Autowired
	WebClient webClient;
	@Autowired
	MemberClient memberClient;
	@Value("${inout.uri}")
	URI inoutUri;
	private Map<String, Map<Integer, String>> outcomeCategories = Collections.emptyMap();
	private Map<Integer, String> incomeCategories = Collections.emptyMap();
	private Map<String, String> members = Collections.emptyMap();

	@PostConstruct
	public void init() {
		Flux<Tuple3<String, Integer, String>> outcomeCategories = outcomeCategories();
		Flux<Tuple2<Integer, String>> incomeCategories = incomeCategories();
		Flux<Member> members = members();

		this.outcomeCategories = outcomeCategories.toStream().collect(groupingBy(
				Tuple2::getT1, LinkedHashMap::new,
				toMap(Tuple3::getT2, Tuple3::getT3, (k, v) -> v, LinkedHashMap::new)));
		this.incomeCategories = incomeCategories.toStream().collect(
				toMap(Tuple2::getT1, Tuple2::getT2, (k, v) -> v, LinkedHashMap::new));
		this.members = members.toStream()
				.collect(toMap(Member::getMemberId,
						m -> m.getFamilyName() + " " + m.getGivenName(), (k, v) -> v,
						LinkedHashMap::new));
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

	private Flux<Tuple3<String, Integer, String>> outcomeCategories() {
		ClientRequest<?> req = ClientRequest.GET(inoutUri + "/outcomeCategories")
				.accept(MediaTypes.HAL_JSON).build();
		return webClient.exchange(req).then(res -> res.bodyToMono(JsonNode.class))
				.flatMap(node -> Flux.fromIterable(
						node.get("_embedded").findPath("outcomeCategories")))
				.map(c -> Tuples.of(
						c.get("parentOutcomeCategory").get("parentCategoryName").asText(),
						c.get("categoryId").asInt(), c.get("categoryName").asText()));
	}

	private Flux<Tuple2<Integer, String>> incomeCategories() {
		ClientRequest<?> req = ClientRequest.GET(inoutUri + "/incomeCategories")
				.accept(MediaTypes.HAL_JSON).build();
		return webClient.exchange(req).then(res -> res.bodyToMono(JsonNode.class))
				.flatMap(node -> Flux
						.fromIterable(node.get("_embedded").findPath("incomeCategories")))
				.map(c -> Tuples.of(c.get("categoryId").asInt(),
						c.get("categoryName").asText()));

	}

	private Flux<Member> members() {
		return Flux.defer(() -> Flux.fromIterable(memberClient.findAll()));
	}
}
