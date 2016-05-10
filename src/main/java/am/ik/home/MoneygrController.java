package am.ik.home;

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

@Controller
public class MoneygrController {
    @Autowired
    OAuth2RestTemplate restTemplate;
    @Autowired
    MoneygrUser user;
    @Value("${inout.uri:http://localhost:7777/api}")
    URI inoutUri;

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
