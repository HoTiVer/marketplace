package org.hotiver.api.Controller;

import org.hotiver.dto.home.HomePageDto;
import org.hotiver.service.HomeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomeService homeService;

    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping()
    public HomePageDto getMainPage() {
        return homeService.getMainPage();
    }

}
