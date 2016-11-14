package am.ik.home;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@FeignClient(name = "outcomes", url = "${inout.uri}", path = "incomes")
public interface IncomeClient {
    @RequestMapping(method = RequestMethod.GET, path = "search/findByIncomeDate")
    Resources<Income> findByIncomeDate(@RequestParam(name = "fromDate") LocalDate fromDate,
                                       @RequestParam(name = "toDate") LocalDate toDate);
}
