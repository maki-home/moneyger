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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.*;

@Controller
public class MoneygrController {
    @Autowired
    OAuth2RestTemplate restTemplate;
    @Autowired
    MoneygrUser user;
    @Value("${inout.uri:http://localhost:7777/api}")
    URI inoutUri;
    @Value("${member.uri:http://localhost:9999/uaa/api}")
    URI memberUri;

    @RequestMapping("/")
    String index() {
        return "index";
    }

    Map<String, String> memberMap(Stream<String> ids) {
        JsonNode members = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(memberUri)
                        .pathSegment("members", "search", "findByIds")
                        .queryParam("ids", ids.distinct().toArray())
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, String> memberMap = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(members.get("_embedded").get("members").elements(), Spliterator.ORDERED), false)
                .collect(toMap(
                        node -> node.get("memberId").asText(),
                        node -> node.get("familyName").asText() + " " + node.get("givenName").asText()
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


    @RequestMapping("/monthly/{outcomeDate}")
    String monthly(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
        List<Outcome.SummaryByDate> summaryByDate = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "reportByDate")
                        .queryParam("fromDate", outcomeDate.with(TemporalAdjusters.firstDayOfMonth()))
                        .queryParam("toDate", outcomeDate.with(TemporalAdjusters.lastDayOfMonth()))
                        .build().toUri()).build(), new ParameterizedTypeReference<List<Outcome.SummaryByDate>>() {
                }
        ).getBody();
        List<Outcome.SummaryByParentCategory> summaryByParentCategory = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "reportByParentCategory")
                        .queryParam("fromDate", outcomeDate.with(TemporalAdjusters.firstDayOfMonth()))
                        .queryParam("toDate", outcomeDate.with(TemporalAdjusters.lastDayOfMonth()))
                        .build().toUri()).build(),
                new ParameterizedTypeReference<List<Outcome.SummaryByParentCategory>>() {
                }
        ).getBody();

        model.addAttribute("summaryByDate", summaryByDate);
        model.addAttribute("summaryByParentCategory", summaryByParentCategory);
        model.addAttribute("total", summaryByDate.stream().mapToLong(Outcome.SummaryByDate::getSubTotal).sum());
        model.addAttribute("user", user);
        return "monthly";
    }


    @RequestMapping("/home")
    String home(Model model) {
        LocalDate now = LocalDate.now();
        model.addAttribute("outcomeDate", now);
        return showOutcomes(model, now);
    }

    @RequestMapping(path = "outcomes/{outcomeDate}")
    String showOutcomes(Model model, @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @PathVariable LocalDate outcomeDate) {
        Resources<Outcome> outcomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri)
                        .pathSegment("outcomes", "search", "findByOutcomeDate")
                        .queryParam("outcomeDate", outcomeDate)
                        .build().toUri()).build(),
                new ParameterizedTypeReference<Resources<Outcome>>() {
                }
        ).getBody();
        Map<String, String> memberMap = memberMap(outcomes.getContent().stream().map(Outcome::getOutcomeBy));
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("total", outcomes.getContent().stream().mapToInt(Outcome::getAmount).sum());
        model.addAttribute("user", user);
        model.addAttribute("categories", categories());
        return "home";
    }

    @RequestMapping(path = "outcomes", method = RequestMethod.POST)
    String registerOutcome(@ModelAttribute Outcome outcome) {
        restTemplate.exchange(RequestEntity.post(UriComponentsBuilder.fromUri(inoutUri).pathSegment("outcomes").build().toUri())
                        .body(outcome),
                new ParameterizedTypeReference<Resource<Outcome>>() {
                });
        return "redirect:/outcomes/" + outcome.getOutcomeDate();
    }
}
