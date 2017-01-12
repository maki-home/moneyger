package am.ik.home;

import static org.springframework.core.ResolvableType.forClassWithGenerics;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.web.reactive.function.BodyExtractors.toMono;
import static org.springframework.web.reactive.function.client.ClientRequest.method;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class OutcomeWebClient implements OutcomeClient {
	private final WebClient webClient;
	private final String inoutUrl;

	public OutcomeWebClient(WebClient webClient, @Value("${inout.uri}") String inoutUrl) {
		this.webClient = webClient;
		this.inoutUrl = inoutUrl;
	}

	@Override
	public Mono<Resources<Outcome>> findByOutcomeDate(LocalDate fromDate,
			LocalDate toDate) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl)
						.pathSegment("outcomes", "search", "findByOutcomeDate")
						.queryParam("fromDate", fromDate).queryParam("toDate", toDate)
						.build().encode().toUri()).accept(MediaTypes.HAL_JSON).build();
		System.out.println(this.webClient.exchange(req)
				.then(res -> res.bodyToMono(JsonNode.class)).block());
		return this.webClient.exchange(req).then(res -> res
				.body(toMono(forClassWithGenerics(Resources.class, Outcome.class))));
	}

	@Override
	public Mono<Resources<Outcome>> findByOutcomeNameContaining(String outcomeName) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl)
						.pathSegment("outcomes", "search", "findByOutcomeNameContaining")
						.queryParam("outcomeName", outcomeName).build().encode().toUri())
								.accept(MediaTypes.HAL_JSON).build();
		return this.webClient.exchange(req).then(res -> res
				.body(toMono(forClassWithGenerics(Resources.class, Outcome.class))));
	}

	@Override
	public Mono<Resources<Outcome>> findByParentCategoryId(Integer parentCategoryId,
			LocalDate fromDate, LocalDate toDate) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl)
						.pathSegment("outcomes", "search", "findByParentCategoryId")
						.queryParam("parentCategoryId", parentCategoryId)
						.queryParam("fromDate", fromDate).queryParam("toDate", toDate)
						.build().encode().toUri()).accept(MediaTypes.HAL_JSON).build();
		return this.webClient.exchange(req).then(res -> res
				.body(toMono(forClassWithGenerics(Resources.class, Outcome.class))));
	}

	@Override
	public Flux<Outcome.SummaryByDate> reportByDate(LocalDate fromDate,
			LocalDate toDate) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl).pathSegment("outcomes", "reportByDate")
						.queryParam("fromDate", fromDate).queryParam("toDate", toDate)
						.build().encode().toUri()).build();
		return this.webClient.exchange(req)
				.flatMap(res -> res.bodyToFlux(Outcome.SummaryByDate.class));
	}

	@Override
	public Flux<Outcome.SummaryByParentCategory> reportByParentCategory(
			LocalDate fromDate, LocalDate toDate) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl).pathSegment("outcomes", "reportByParentCategory")
						.queryParam("fromDate", fromDate).queryParam("toDate", toDate)
						.build().encode().toUri()).build();
		return this.webClient.exchange(req)
				.flatMap(res -> res.bodyToFlux(Outcome.SummaryByParentCategory.class));
	}
}
