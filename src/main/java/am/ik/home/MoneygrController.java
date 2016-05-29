package am.ik.home;

import com.fasterxml.jackson.databind.JsonNode;
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
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

@Controller
public class MoneygrController {
    @Autowired
    OAuth2RestTemplate restTemplate;
    @Autowired
    MoneygrUser user;
    @Value("${inout.uri}")
    URI inoutUri;
    @Value("${member.uri}")
    URI memberUri;

    @RequestMapping("/")
    String index() {
        return "index";
    }

    Map<String, String> memberMap(Collection<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        JsonNode members = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(memberUri)
                        .pathSegment("members", "search", "findByIds")
                        .queryParam("ids", ids.toArray())
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, String> memberMap = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(members.get("_embedded").get("members").elements(), Spliterator.ORDERED), false)
                .collect(toMap(
                        node -> node.get("memberId").asText(),
                        node -> node.get("givenName").asText()
                ));
        return memberMap;
    }

    Map<String, Map<Integer, String>> categories() {
        JsonNode categories = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomeCategories")
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, Map<Integer, String>> cat = new LinkedHashMap<>();
        for (JsonNode node : categories.get("_embedded").get("outcomeCategories")) {
            String key = node.get("parentOutcomeCategory").get("parentCategoryName").asText();
            cat.computeIfAbsent(key, x -> new LinkedHashMap<>());
            cat.get(key).put(node.get("categoryId").asInt(), node.get("categoryName").asText());
        }
        return cat;
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
        Map<String, String> memberMap = memberMap(outcomes.getContent().stream().map(Outcome::getOutcomeBy).distinct().collect(Collectors.toList()));
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", categories());
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

        Map<String, String> memberMap = memberMap(outcomes.getContent().stream().map(Outcome::getOutcomeBy).distinct().collect(Collectors.toList()));
        outcomes.forEach(o -> o.setMemberMap(memberMap));

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", to);
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(o -> o.getAmount() * o.getQuantity()).sum());
        model.addAttribute("user", user);
        Map<String, Map<Integer, String>> categories = categories();
        model.addAttribute("categories", categories);
        model.addAttribute("parentCategory", categories.entrySet().stream().map(Map.Entry::getKey).toArray()[parentCategoryId - 1]);
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
}
