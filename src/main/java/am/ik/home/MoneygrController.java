package am.ik.home;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resource;
import org.springframework.hateoas.Resources;
import org.springframework.http.RequestEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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

    @RequestMapping("/home")
    String home(Model model) {
        Resources<Outcome> outcomes = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(inoutUri).pathSegment("outcomes").build().toUri()).build(),
                new ParameterizedTypeReference<Resources<Outcome>>() {
                }
        ).getBody();
        JsonNode members = restTemplate.exchange(
                RequestEntity.get(UriComponentsBuilder.fromUri(memberUri)
                        .pathSegment("members", "search", "findByIds")
                        .queryParam("ids", outcomes.getContent().stream().map(Outcome::getOutcomeBy).distinct().toArray())
                        .build().toUri()).build(),
                JsonNode.class)
                .getBody();
        Map<String, String> memberMap = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(members.get("_embedded").get("members").elements(), Spliterator.ORDERED), false)
                .collect(Collectors.toMap(
                        node -> node.get("memberId").asText(),
                        node -> node.get("familyName").asText() + " " + node.get("givenName").asText()
                ));
        outcomes.forEach(o -> o.setMemberMap(memberMap));
        model.addAttribute("outcomes", outcomes);
        model.addAttribute("user", user);
        return "home";
    }

    @RequestMapping(path = "outcomes", method = RequestMethod.POST)
    String registerOutcome(@ModelAttribute Outcome outcome) {
        restTemplate.exchange(RequestEntity.post(UriComponentsBuilder.fromUri(inoutUri).pathSegment("outcomes").build().toUri())
                        .body(outcome),
                new ParameterizedTypeReference<Resource<Outcome>>() {
                });
        return "redirect:/home";
    }
}
