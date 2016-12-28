package am.ik.home;

import java.time.LocalDate;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "outcomes", url = "${inout.uri}", path = "incomes")
public interface IncomeClient {
	@RequestMapping(method = RequestMethod.GET, path = "search/findByIncomeDate")
	Resources<Income> findByIncomeDate(
			@RequestParam(name = "fromDate") LocalDate fromDate,
			@RequestParam(name = "toDate") LocalDate toDate);
}
