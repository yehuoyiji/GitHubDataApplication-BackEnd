package com.yehuo.githubdatabackend.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yehuo.githubdatabackend.entity.PersonVo;
import com.yehuo.githubdatabackend.entity.ResponseResult;
import com.yehuo.githubdatabackend.entity.SearchConditionDto;
import com.yehuo.githubdatabackend.entity.SearchConditionVo;
import com.yehuo.githubdatabackend.enums.AppHttpCodeEnum;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import cn.hutool.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


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

    @GetMapping("/getAllUserInformation")
    public ResponseResult getAllUserInformation(@RequestBody SearchConditionDto searchConditionDto) {
        return GitHubLocationQuery(searchConditionDto);
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
            PersonVo personVo = JSONUtil.toBean(execute.body(), PersonVo.class);
            return ResponseResult.okResult(personVo);
        }
    }

    public ResponseResult GitHubLocationQuery(SearchConditionDto searchConditionDto) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        SearchConditionVo searchConditionVo = null;
        // 1. 先根据所在地拿到所有的开发者
        HttpRequest request = HttpRequest.get("https://api.github.com/search/users?q=location:" + searchConditionDto.getCountry() + "&per_page=" + searchConditionDto.getPageSize() + "&page=" + searchConditionDto.getPageNum())
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + apiToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse execute = request.execute();
        int status = execute.getStatus();
        if (status != 200) {
            return ResponseResult.errorResult(404, "请输入正确的用户名");
        }else {
            searchConditionVo = JSONUtil.toBean(execute.body(), SearchConditionVo.class);
        }
        // 2. 根据已拿到的开发者列表，再根据开发者的仓库进行语言的筛选
        List<PersonVo> userList = searchConditionVo.getItems();
        List<PersonVo> resList = new ArrayList<>();
        List<String> repoList = userList.stream().map(PersonVo::getRepos_url).collect(Collectors.toList());
        CompletableFuture<?>[] futures = new CompletableFuture<?>[repoList.size()];
        for(int i = 0; i < repoList.size(); i++) {
            int finalI = i;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                HttpResponse response = HttpRequest.get(repoList.get(finalI))
                        .header("Accept", "application/vnd.github+json")
                        .header("Authorization", "Bearer " + apiToken)
                        .header("X-GitHub-Api-Version", "2022-11-28")
                        .execute();
                if (response.isOk()) {
                    JSONArray jsonArray = JSONUtil.parseArray(response.body());
                    int languageCount = 0;
                    loop: for (Object repoObj: jsonArray) {
                        JSONObject repo = (JSONObject) repoObj;
                        String language = repo.getStr("language");
                        if(searchConditionDto.getLanguage().equalsIgnoreCase(language)) {
                            languageCount++;
                            if (languageCount > jsonArray.size() / 3) {
                                for(PersonVo p: userList) {
                                    if (p.getRepos_url().equals(repoList.get(finalI))) {
                                        resList.add(p);
                                        break loop;
                                    }
                                }
                            }
                        }
                    }
                }

            });
            futures[finalI] = future;
        }
        CompletableFuture.allOf(futures).join(); // 等待所有任务完成
        CompletableFuture<?>[] endFutures = new CompletableFuture<?>[resList.size()];
        List<PersonVo> result = new ArrayList<>();
        for(int i = 0; i < resList.size(); i++) {
            int finalI = i;
            CompletableFuture<?> future = CompletableFuture.runAsync(() -> {
                HttpRequest httpRequest = HttpRequest.get("https://api.github.com/users/" + resList.get(finalI).getLogin())
                        .header("Accept", "application/vnd.github+json")
                        .header("Authorization", "Bearer " + apiToken)
                        .header("X-GitHub-Api-Version", "2022-11-28");
                HttpResponse httpResponse = httpRequest.execute();
                PersonVo personVo = JSONUtil.toBean(httpResponse.body(), PersonVo.class);
                result.add(personVo);
            });
            endFutures[finalI] = future;
        }
        CompletableFuture.allOf(endFutures).join();
        stopWatch.stop();
        System.out.println("耗时：" + stopWatch.getLastTaskTimeMillis());
        return ResponseResult.okResult(result);

    }
}
