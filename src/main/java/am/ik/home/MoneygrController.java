package am.ik.home;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import am.ik.home.client.user.UaaUser;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Controller
public class MoneygrController {
	@Autowired
	OutcomeClient outcomeClient;
	@Autowired
	IncomeClient incomeClient;
	@Autowired
	OutcomeSender outcomeSender;
	@Autowired
	IncomeSender incomeSender;
	@Autowired
	UaaUser user;
	@Autowired
	SessionCache cache;
	@Value("${inout.uri}")
	URI inoutUri;

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
	String showOutcomes(Model model,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
		return showOutcomes(model, outcomeDate, Optional.of(outcomeDate));
	}

	@RequestMapping(path = "outcomes", params = "fromDate")
	String showOutcomes(Model model,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		Map<String, String> memberMap = cache.getMembers();
		Flux<Outcome> outcomes = outcomeClient.findByOutcomeDate(fromDate, to)
				.flatMap(r -> Flux.fromIterable(r.getContent())).map(x -> {
					x.setMemberMap(memberMap);
					return x;
				});
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomes", outcomes.toIterable());
		model.addAttribute("total",
				outcomes.toStream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user);
		model.addAttribute("categories", cache.getOutcomeCategories());
		model.addAttribute("members", cache.getMembers());
		return "outcomes";
	}

	@RequestMapping(path = "outcomes", params = "keyword")
	String searchOutcomes(Model model, @RequestParam String keyword) throws IOException {
		Map<String, String> memberMap = cache.getMembers();
		Flux<Outcome> outcomes = outcomeClient
				.findByOutcomeNameContaining(UriUtils.encodeQueryParam(keyword, "UTF-8"))
				.flatMap(r -> Flux.fromIterable(r.getContent())).map(x -> {
					x.setMemberMap(memberMap);
					return x;
				});
		model.addAttribute("outcomes", outcomes.toIterable());
		model.addAttribute("total",
				outcomes.toStream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user);
		model.addAttribute("categories", cache.getOutcomeCategories());
		model.addAttribute("members", cache.getMembers());
		return "outcomes";
	}

	@RequestMapping(path = "outcomes", params = { "parentCategoryId", "fromDate" })
	String showOutcomesByParentCategoryId(Model model,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate,
			@RequestParam Integer parentCategoryId) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		Map<String, String> memberMap = cache.getMembers();
		Flux<Outcome> outcomes = outcomeClient
				.findByParentCategoryId(parentCategoryId, fromDate, to)
				.flatMap(r -> Flux.fromIterable(r.getContent())).map(x -> {
					x.setMemberMap(memberMap);
					return x;
				});

		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomes", outcomes.toIterable());
		model.addAttribute("total",
				outcomes.toStream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
		model.addAttribute("user", user);
		Map<String, Map<Integer, String>> categories = cache.getOutcomeCategories();
		model.addAttribute("categories", categories);
		model.addAttribute("parentCategory", categories.entrySet().stream()
				.map(Map.Entry::getKey).toArray()[parentCategoryId - 1]);
		model.addAttribute("members", cache.getMembers());
		return "outcomes";
	}

	@RequestMapping(path = "outcomes", method = RequestMethod.POST)
	String registerOutcome(@Validated Outcome outcome, BindingResult result, Model model,
			HttpServletResponse response) {
		if (result.hasErrors()) {
			return outcome.getOutcomeDate() == null ? showOutcomes(model)
					: showOutcomes(model, outcome.getOutcomeDate());
		}
		if (StringUtils.isEmpty(outcome.getOutcomeBy())) {
			outcome.setOutcomeBy(user.getUserId());
		}
		outcomeSender.send(outcome);
		Cookie cookie = new Cookie("creditCard", String.valueOf(outcome.isCreditCard()));
		response.addCookie(cookie);
		return "redirect:/outcomes/" + outcome.getOutcomeDate();
	}

	@RequestMapping(path = "incomes")
	String showIncomes(Model model) {
		return showIncomes(model,
				LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
				Optional.empty());
	}

	@RequestMapping(path = "incomes", params = "fromDate")
	String showIncomes(Model model,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));

		Map<String, String> memberMap = cache.getMembers();
		Flux<Income> incomes = incomeClient.findByIncomeDate(fromDate, to)
				.flatMap(r -> Flux.fromIterable(r.getContent())).map(x -> {
					x.setMemberMap(memberMap);
					return x;
				});
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("incomes", incomes.toIterable());
		model.addAttribute("total", incomes.toStream().mapToInt(Income::getAmount).sum());
		model.addAttribute("user", user);
		model.addAttribute("categories", cache.getIncomeCategories());
		model.addAttribute("members", cache.getMembers());
		return "incomes";
	}

	@RequestMapping(path = "incomes", method = RequestMethod.POST)
	String registerIncome(@Validated Income income, BindingResult result, Model model,
			RedirectAttributes attributes) {
		if (result.hasErrors()) {
			return income.getIncomeDate() == null ? showIncomes(model)
					: showIncomes(model, income.getIncomeDate(), Optional.empty());
		}
		if (StringUtils.isEmpty(income.getIncomeBy())) {
			income.setIncomeBy(user.getUserId());
		}
		incomeSender.send(income);
		LocalDate date = income.getIncomeDate().with(TemporalAdjusters.firstDayOfMonth());
		attributes.addAttribute("fromDate",
				date.format(DateTimeFormatter.ISO_LOCAL_DATE));
		return "redirect:/incomes";
	}

	@RequestMapping(path = "/report")
	String report(Model model) {
		return report(model, LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()),
				Optional.empty());
	}

	@RequestMapping(path = "/report", params = "fromDate")
	String report(Model model,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam LocalDate fromDate,
			@DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam Optional<LocalDate> toDate) {
		LocalDate to = toDate
				.orElseGet(() -> fromDate.with(TemporalAdjusters.lastDayOfMonth()));
		Flux<Outcome.SummaryByDate> summaryByDate = outcomeClient.reportByDate(fromDate,
				to);
		Flux<Outcome.SummaryByParentCategory> summaryByParentCategory = outcomeClient
				.reportByParentCategory(fromDate, to);
		// TODO use Report API
		Flux<Income> incomes = incomeClient.findByIncomeDate(fromDate, to)
				.flatMap(r -> Flux.fromIterable(r.getContent()));

		Mono<Long> outcomeTotal = summaryByDate.reduce(0L, (x, s) -> x + s.getSubTotal())
				.otherwiseReturn(0L);
		Mono<Long> incomeTotal = incomes.reduce(0L, (x, s) -> x + s.getAmount());

		summaryByDate.collect(Collectors.toList()).subscribe(x -> {
			if (x.size() <= 31) {
				model.addAttribute("outcomeSummaryByDate", x);
			}
			else {
				model.addAttribute("outcomeSummaryByMonth", summarizeByMonth(x));
			}
		});
		model.addAttribute("fromDate", fromDate);
		model.addAttribute("toDate", to);
		model.addAttribute("outcomeSummaryByParentCategory",
				summaryByParentCategory.toIterable());
		model.addAttribute("outcomeTotal", outcomeTotal.block());
		model.addAttribute("incomeTotal", incomeTotal.block());
		model.addAttribute("inout", incomeTotal.block() - outcomeTotal.block());
		model.addAttribute("user", user);
		return "report";
	}

	List<Outcome.SummaryByDate> summarizeByMonth(
			Collection<Outcome.SummaryByDate> outcomes) {
		return outcomes.stream()
				.collect(Collectors
						.groupingBy(x -> LocalDate.of(x.getOutcomeDate().getYear(),
								x.getOutcomeDate().getMonth(), 1)))
				.entrySet().stream()
				.map(x -> new Outcome.SummaryByDate(x.getKey(),
						x.getValue().stream().map(Outcome.SummaryByDate::getSubTotal)
								.mapToLong(Long::longValue).sum()))
				.sorted(Comparator.comparing(Outcome.SummaryByDate::getOutcomeDate)
						.reversed())
				.collect(Collectors.toList());
	}

	@ModelAttribute
	Outcome outcome(
			@CookieValue(name = "creditCard", defaultValue = "false") boolean isCreditCard) {
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
