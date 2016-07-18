package am.ik.home;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "outcomes", url = "${inout.uri}", path = "outcomes")
public interface OutcomeClient {
    @RequestMapping(method = RequestMethod.GET, path = "search/findByOutcomeDate")
    Resources<Outcome> findByOutcomeDate(@RequestParam(name = "fromDate") LocalDate fromDate,
                                         @RequestParam(name = "toDate") LocalDate toDate);

    @RequestMapping(method = RequestMethod.GET, path = "search/findByOutcomeNameContaining")
    Resources<Outcome> findByOutcomeNameContaining(@RequestParam(name = "outcomeName") String outcomeName);

    @RequestMapping(method = RequestMethod.GET, path = "search/findByParentCategoryId")
    Resources<Outcome> findByParentCategoryId(@RequestParam(name = "parentCategoryId") Integer parentCategoryId,
                                              @RequestParam(name = "fromDate") LocalDate fromDate,
                                              @RequestParam(name = "toDate") LocalDate toDate);

    @RequestMapping(method = RequestMethod.GET, path = "reportByDate")
    List<Outcome.SummaryByDate> reportByDate(@RequestParam(name = "fromDate") LocalDate fromDate,
                                             @RequestParam(name = "toDate") LocalDate toDate);

    @RequestMapping(method = RequestMethod.GET, path = "reportByParentCategory")
    List<Outcome.SummaryByParentCategory> reportByParentCategory(@RequestParam(name = "fromDate") LocalDate fromDate,
                                                                 @RequestParam(name = "toDate") LocalDate toDate);

    @RequestMapping(method = RequestMethod.POST)
    Outcome postOutcome(Outcome outcome);
}
