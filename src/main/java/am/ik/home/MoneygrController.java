package am.ik.home;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.Resources;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class MoneygrController {
    @Autowired
    OutcomeClient outcomeClient;
    @Autowired
    IncomeClient incomeClient;
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

    //@RequestMapping(path = "outcomes")
    String showOutcomes(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("outcomeDate", now);
        return showOutcomes(model, now);
    }

    //@RequestMapping(path = "outcomes/{outcomeDate}")
    String showOutcomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
        return showOutcomes(model, outcomeDate, Optional.of(outcomeDate));
    }

    //@RequestMapping(path = "outcomes", params = "fromDate")
    String showOutcomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
        Resources<Outcome> outcomes = outcomeClient.findByOutcomeDate(fromDate, to);
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

    //@RequestMapping(path = "outcomes", params = "keyword")
    String searchOutcomes(Model model, @RequestParam String keyword) throws IOException {
        Resources<Outcome> outcomes = outcomeClient.findByOutcomeNameContaining(UriUtils.encodeQueryParam(keyword, "UTF-8"));
        Map<String, String> memberMap = cache.getMembers();
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", cache.getOutcomeCategories());
        model.addAttribute("members", cache.getMembers());
        return "outcomes";
    }

    //@RequestMapping(path = "outcomes", params = {"parentCategoryId", "fromDate"})
    String showOutcomesByParentCategoryId(Model model,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                                          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
                                          @RequestParam Integer parentCategoryId) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

        Resources<Outcome> outcomes = outcomeClient.findByParentCategoryId(parentCategoryId, fromDate, to);

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

    //@RequestMapping(path = "outcomes", method = RequestMethod.POST)
    String registerOutcome(@Validated Outcome outcome, BindingResult result, Model model, HttpServletResponse response) {
        if (result.hasErrors()) {
            return outcome.getOutcomeDate() == null ? showOutcomes(model) : showOutcomes(model, outcome.getOutcomeDate());
        }
        outcomeClient.postOutcome(outcome);
        Cookie cookie = new Cookie("creditCard", String.valueOf(outcome.isCreditCard()));
        response.addCookie(cookie);
        return "redirect:/outcomes/" + outcome.getOutcomeDate();
    }

    //@RequestMapping(path = "incomes")
    String showIncomes(Model model) {
        return showIncomes(model, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), Optional.empty());
    }

    //@RequestMapping(path = "incomes", params = "fromDate")
    String showIncomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

        Resources<Income> incomes = incomeClient.findByIncomeDate(fromDate, to);
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


    //@RequestMapping(path = "incomes", method = RequestMethod.POST)
    String registerIncome(@Validated Income income, BindingResult result, Model model, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return income.getIncomeDate() == null ? showIncomes(model) : showIncomes(model, income.getIncomeDate(), Optional.empty());
        }
        incomeClient.postIncome(income);
        LocalDate date = income.getIncomeDate().with(TemporalAdjusters.firstDayOfMonth());
        attributes.addAttribute("fromDate", date.format(DateTimeFormatter.ISO_LOCAL_DATE));
        return "redirect:/incomes";
    }

    //@RequestMapping(path = "/report")
    String report(Model model) {
        return report(model, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()), Optional.empty());
    }

    //@RequestMapping(path = "/report", params = "fromDate")
    String report(Model model,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
                  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
        LocalDate to = toDate.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
        List<Outcome.SummaryByDate> summaryByDate = outcomeClient.reportByDate(fromDate, to);
        List<Outcome.SummaryByParentCategory> summaryByParentCategory = outcomeClient.reportByParentCategory(fromDate, to);
        // TODO use Report API
        Resources<Income> incomes = incomeClient.findByIncomeDate(fromDate, to);

        long outcomeTotal = summaryByDate.stream().mapToLong(Outcome.SummaryByDate::getSubTotal).sum();
        long incomeTotal = incomes.getContent().stream().mapToLong(Income::getAmount).sum();
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        if (summaryByDate.size() <= 31) {
            model.addAttribute("outcomeSummaryByDate", summaryByDate);
        } else {
            model.addAttribute("outcomeSummaryByMonth", summarizeByMonth(summaryByDate));
        }
        model.addAttribute("outcomeSummaryByParentCategory", summaryByParentCategory);
        model.addAttribute("outcomeTotal", outcomeTotal);
        model.addAttribute("incomeTotal", incomeTotal);
        model.addAttribute("inout", incomeTotal - outcomeTotal);
        model.addAttribute("user", user);
        return "report";
    }

    List<Outcome.SummaryByDate> summarizeByMonth(List<Outcome.SummaryByDate> outcomes) {
        return outcomes.stream()
                .collect(Collectors.groupingBy(x -> LocalDate.of(x.getOutcomeDate().getYear(), x.getOutcomeDate().getMonth(), 1)))
                .entrySet()
                .stream()
                .map(x -> new Outcome.SummaryByDate(x.getKey(),
                        x.getValue().stream().map(Outcome.SummaryByDate::getSubTotal).mapToLong(Long::longValue).sum()))
                .sorted(Comparator.comparing(Outcome.SummaryByDate::getOutcomeDate).reversed())
                .collect(Collectors.toList());
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
