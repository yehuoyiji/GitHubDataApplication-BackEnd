package com.yehuo.githubdatabackend.controller;

import cn.hutool.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import cn.hutool.http.HttpRequest;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Value("${github.apiToken}")
    private String apiToken;

    @GetMapping("/getPersonalInformation/{githubName}")
    public String getPersonalInformation(@PathVariable("githubName") String githubName) {
        return PersonalInformation(githubName);
    }

    public String PersonalInformation(String githubName) {
        HttpRequest request = HttpRequest.get("https://api.github.com/users/" + githubName)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + apiToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse execute = request.execute();
        return execute.body();
    }


}
