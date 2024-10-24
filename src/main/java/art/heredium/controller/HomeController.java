package art.heredium.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Profile({"local", "stage"})
@Controller
public class HomeController {
  @GetMapping("/")
  public String goHome() {
    return "redirect:/swagger-ui/index.html";
  }
}
