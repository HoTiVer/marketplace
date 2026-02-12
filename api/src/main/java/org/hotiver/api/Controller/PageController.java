package org.hotiver.api.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/")
    public String getMainPage() {
        return "index.html";
    }
}
