package org.hotiver.api.Controller;

import org.hotiver.dto.home.HomePageDto;
import org.hotiver.service.homepage.HomePageService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomePageService homePageService;

    public HomeController(HomePageService homePageService) {
        this.homePageService = homePageService;
    }

    @GetMapping()
    public HomePageDto getMainPage() {
        return homePageService.getMainPage();
    }

}
