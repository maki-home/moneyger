package am.ik.home;

import java.time.LocalDate;

import org.springframework.hateoas.Resources;

import reactor.core.publisher.Mono;

public interface IncomeClient {
	Mono<Resources<Income>> findByIncomeDate(LocalDate fromDate, LocalDate toDate);
}
