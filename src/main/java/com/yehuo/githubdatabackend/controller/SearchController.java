package com.yehuo.githubdatabackend.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONUtil;
import com.yehuo.githubdatabackend.entity.Person;
import com.yehuo.githubdatabackend.entity.ResponseResult;
import com.yehuo.githubdatabackend.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import cn.hutool.http.HttpRequest;

import java.util.Objects;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Value("${github.apiToken}")
    private String apiToken;

    @GetMapping("/getPersonalInformation/{githubName}")
    public ResponseResult getPersonalInformation(@PathVariable("githubName") String githubName) {
        if (!StringUtils.hasText(githubName) || Objects.isNull(githubName)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_NOT_NULL);
        }
        return PersonalInformation(githubName);
    }

    public ResponseResult PersonalInformation(String githubName) {
        HttpRequest request = HttpRequest.get("https://api.github.com/users/" + githubName)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + apiToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse execute = request.execute();
        int status = execute.getStatus();
        if (status != 200) {
            return ResponseResult.errorResult(404, "请输入正确的用户名");
        }else {
            Person person = JSONUtil.toBean(execute.body(), Person.class);
            return ResponseResult.okResult(person);
        }
    }
}
