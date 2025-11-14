package org.hotiver.api;

import org.hotiver.dto.home.HomePageDto;
import org.hotiver.service.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/api/home")
    public HomePageDto getMainPage() {
        return homeService.getMainPage();
    }

}
