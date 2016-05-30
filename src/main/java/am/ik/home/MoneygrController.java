package am.ik.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

@Controller
public class MoneygrController {
    @Autowired
    OAuth2RestTemplate restTemplate;
    @Autowired
    MoneygrUser user;
    @Autowired
    SessionCache cache;
    @Value("${inout.uri}")
    URI inoutUri;
    @Value("${member.uri}")
    URI memberUri;

    @RequestMapping("/")
    String index() {
        return "index";
    }

    @RequestMapping(path = "outcomes")
    String showOutcomes(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("outcomeDate", now);
        return showOutcomes(model, now);
    }

    @RequestMapping(path = "outcomes/{outcomeDate}")
    String showOutcomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
        return showOutcomes(model, outcomeDate, Optional.of(outcomeDate));
    }

    @RequestMapping(path = "outcomes", params = "fromDate")
    String showOutcomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

        Resources<Outcome> outcomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "search", "findByOutcomeDate")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", to)
                        .build().toUri()).build(),
                new ParameterizedTypeReference<Resources<Outcome>>() {
                }
        ).getBody();
        Map<String, String> memberMap = cache.getMembers();
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", cache.getOutcomeCategories());
        model.addAttribute("members", cache.getMembers());
        return "outcomes";
    }

    @RequestMapping(path = "outcomes", params = "keyword")
    String searchOutcomes(Model model, @RequestParam String keyword) throws IOException {
        Resources<Outcome> outcomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "search", "findByOutcomeNameContaining")
                        .queryParam("outcomeName", UriUtils.encodeQueryParam(keyword, "UTF-8"))
                        .build(true).toUri()).build(),
                new ParameterizedTypeReference<Resources<Outcome>>() {
                }
        ).getBody();
        Map<String, String> memberMap = cache.getMembers();
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", cache.getOutcomeCategories());
        model.addAttribute("members", cache.getMembers());
        return "outcomes";
    }

    @RequestMapping(path = "outcomes", params = {"parentCategoryId", "fromDate"})
    String showOutcomesByParentCategoryId(Model model,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
                                          @RequestParam Integer parentCategoryId) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

        Resources<Outcome> outcomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "search", "findByParentCategoryId")
                        .queryParam("parentCategoryId", parentCategoryId)
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", to)
                        .build().toUri()).build(), new ParameterizedTypeReference<Resources<Outcome>>() {
                }
        ).getBody();

        Map<String, String> memberMap = cache.getMembers();
        outcomes.forEach(o -> o.setMemberMap(memberMap));

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        Map<String, Map<Integer, String>> categories = cache.getOutcomeCategories();
        model.addAttribute("categories", categories);
        model.addAttribute("parentCategory", categories.entrySet().stream().map(Map.Entry::getKey).toArray()[parentCategoryId - 1]);
        model.addAttribute("members", cache.getMembers());
        return "outcomes";
    }

    @RequestMapping(path = "outcomes", method = RequestMethod.POST)
    String registerOutcome(@Validated Outcome outcome, BindingResult result, Model model, HttpServletResponse response) {
        if (result.hasErrors()) {
            return outcome.getOutcomeDate() == null ? showOutcomes(model) : showOutcomes(model, outcome.getOutcomeDate());
        }
        RequestEntity<Outcome> req = RequestEntity.post(UriComponentsBuilder.fromUri(inoutUri)
                .pathSegment("outcomes")
                .build().toUri()).body(outcome);
        restTemplate.exchange(req, new ParameterizedTypeReference<Resource<Outcome>>() {
        });
        Cookie cookie = new Cookie("creditCard", String.valueOf(outcome.isCreditCard()));
        response.addCookie(cookie);
        return "redirect:/outcomes/" + outcome.getOutcomeDate();
    }

    @RequestMapping(path = "incomes")
    String showIncomes(Model model) {
        return showIncomes(model, LocalDate.now(), Optional.empty());
    }

    @RequestMapping(path = "incomes", params = "fromDate")
    String showIncomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

        Resources<Income> incomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("incomes", "search", "findByIncomeDate")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", to)
                        .build().toUri()).build(),
                new ParameterizedTypeReference<Resources<Income>>() {
                }
        ).getBody();
        Map<String, String> memberMap = cache.getMembers();
        incomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("incomes", incomes);
        model.addAttribute("total", incomes.getContent().stream().mapToInt(Income::getAmount).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", cache.getIncomeCategories());
        model.addAttribute("members", cache.getMembers());
        return "incomes";
    }


    @RequestMapping(path = "incomes", method = RequestMethod.POST)
    String registerIncome(@Validated Income income, BindingResult result, Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return income.getIncomeDate() == null ? showIncomes(model) : showIncomes(model, income.getIncomeDate(), Optional.empty());
        }
        RequestEntity<Income> req = RequestEntity.post(UriComponentsBuilder.fromUri(inoutUri)
                .pathSegment("incomes")
                .build().toUri()).body(income);
        restTemplate.exchange(req, new ParameterizedTypeReference<Resource<Income>>() {
        });
        LocalDate date = income.getIncomeDate().with(TemporalAdjusters.firstDayOfMonth());
        attributes.addAttribute("fromDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return "redirect:/incomes";
    }

    @RequestMapping(path = "/report")
    String report(Model model) {
        return report(model, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), Optional.empty());
    }

    @RequestMapping(path = "/report", params = "fromDate")
    String report(Model model,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
        List<Outcome.SummaryByDate> summaryByDate = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "reportByDate")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", to)
                        .build().toUri()).build(), new ParameterizedTypeReference<List<Outcome.SummaryByDate>>() {
                }
        ).getBody();
        List<Outcome.SummaryByParentCategory> summaryByParentCategory = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "reportByParentCategory")
                        .queryParam("fromDate", fromDate)
                        .queryParam("toDate", to)
                        .build().toUri()).build(),
                new ParameterizedTypeReference<List<Outcome.SummaryByParentCategory>>() {
                }
        ).getBody();

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("summaryByDate", summaryByDate);
        model.addAttribute("summaryByParentCategory", summaryByParentCategory);
        model.addAttribute("total", summaryByDate.stream().mapToLong(Outcome.SummaryByDate::getSubTotal).sum());
        model.addAttribute("user", user);
        return "report";
    }

    @ModelAttribute
    Outcome outcome(@CookieValue(name = "creditCard", defaultValue = "false") boolean isCreditCard) {
        Outcome outcome = new Outcome();
        outcome.setCreditCard(isCreditCard);
        return outcome;
    }

    @ModelAttribute
    Income income() {
        Income income = new Income();
        income.setIncomeDate(LocalDate.now());
        return income;
    }
}
