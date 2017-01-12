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

import reactor.core.publisher.Mono;

@Component
public class IncomeWebClient implements IncomeClient {
	private final WebClient webClient;
	private final String inoutUrl;

	public IncomeWebClient(WebClient webClient, @Value("${inout.uri}") String inoutUrl) {
		this.webClient = webClient;
		this.inoutUrl = inoutUrl;
	}

	@Override
	public Mono<Resources<Income>> findByIncomeDate(LocalDate fromDate,
			LocalDate toDate) {
		ClientRequest<Void> req = method(GET,
				fromHttpUrl(inoutUrl).pathSegment("incomes", "search", "findByIncomeDate")
						.queryParam("fromDate", fromDate).queryParam("toDate", toDate)
						.build().encode().toUri()).accept(MediaTypes.HAL_JSON).build();
		return this.webClient.exchange(req).then(res -> res
				.body(toMono(forClassWithGenerics(Resources.class, Income.class))));
	}
}
