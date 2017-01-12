package am.ik.home;

import java.time.LocalDate;

import org.springframework.hateoas.Resources;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OutcomeClient {
	Mono<Resources<Outcome>> findByOutcomeDate(LocalDate fromDate, LocalDate toDate);

	Mono<Resources<Outcome>> findByOutcomeNameContaining(String outcomeName);

	Mono<Resources<Outcome>> findByParentCategoryId(Integer parentCategoryId,
			LocalDate fromDate, LocalDate toDate);

	Flux<Outcome.SummaryByDate> reportByDate(LocalDate fromDate, LocalDate toDate);

	Flux<Outcome.SummaryByParentCategory> reportByParentCategory(LocalDate fromDate,
			LocalDate toDate);
}
