package com.yehuo.githubdatabackend.controller;

import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yehuo.githubdatabackend.entity.*;
import com.yehuo.githubdatabackend.enums.AppHttpCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import cn.hutool.http.HttpRequest;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/search")
public class SearchController {

    @Value("${github.apiToken}")
    private String apiToken;

    @Autowired
    private RedisTemplate redisTemplate;

    public double confidenceLevel = 0;
    @GetMapping("/getPersonalInformation/{githubName}")
    public ResponseResult getPersonalInformation(@PathVariable("githubName") String githubName) {
        if (!StringUtils.hasText(githubName) || Objects.isNull(githubName)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.USERNAME_NOT_NULL);
        }
        PersonVo personVo = (PersonVo) redisTemplate.opsForValue().get(githubName);
        if (Objects.isNull(personVo)) {
            return PersonalInformation(githubName);
        }
        return ResponseResult.okResult(personVo);
    }

    @PostMapping("/getAllUserInformation")
    public ResponseResult getAllUserInformation(@RequestBody SearchConditionDto searchConditionDto) {
        System.out.println(searchConditionDto.toString());
        Object res = redisTemplate.opsForValue().get(searchConditionDto.toString());
        if (Objects.isNull(res)) {
            return GitHubLocationQuery(searchConditionDto);
        } else {
            return ResponseResult.okResult(res);
        }

    }


    public ResponseResult PersonalInformation(String githubName) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        HttpRequest request = HttpRequest.get("https://api.github.com/users/" + githubName)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + apiToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse execute = request.execute();
        int status = execute.getStatus();
        if (status != 200) {
            return ResponseResult.errorResult(404, execute.body());
        }else {
            PersonVo personVo = JSONUtil.toBean(execute.body(), PersonVo.class);
            redisTemplate.opsForValue().set(githubName, personVo, 5, TimeUnit.MINUTES);
            stopWatch.stop();
            System.out.println("耗时：" + stopWatch.getLastTaskTimeMillis());
            return ResponseResult.okResult(personVo);
        }
    }

    @GetMapping("/getRepositoryByName/{userName}")
    public List getRepositoryByName(@PathVariable("userName")String userName) {
        HttpRequest request = HttpRequest.get("https://api.github.com/users/" + userName + "/repos")
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", "Bearer " + apiToken)
                .header("X-GitHub-Api-Version", "2022-11-28");
        HttpResponse execute = request.execute();
        int status = execute.getStatus();
        if (status != 200) {
            return new ArrayList<Repository>();
        }else {
            List<Repository> result = JSONUtil.toList(execute.body(), Repository.class);
            double score = calculateDeveloperScore(result);
            List res = new ArrayList();
            res.add(result);
            res.add(score);
            return res;

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
            return ResponseResult.errorResult(404, execute.body());
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
                    // 针对用户仓库进行评分
                    List<Repository> list = (List<Repository>) JSONUtil.toList(response.body(), Repository.class);
                    double score = calculateDeveloperScore(list);

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
                                        p.setScore(score);
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

                Double score = resList.get(finalI).getScore();
                double finalScore = calculatePercentageScore(score);
                PersonVo personVo = JSONUtil.toBean(httpResponse.body(), PersonVo.class);
                DecimalFormat df = new DecimalFormat("#.00");
                personVo.setScore(Double.valueOf(df.format(finalScore)));
                result.add(personVo);
            });
            endFutures[finalI] = future;
        }
        CompletableFuture.allOf(endFutures).join();
        stopWatch.stop();
        redisTemplate.opsForValue().set(searchConditionDto.toString(), result, 10, TimeUnit.MINUTES);
        System.out.println("耗时：" + stopWatch.getLastTaskTimeMillis());
        return ResponseResult.okResult(result);
    }

    public static double calculateDeveloperScore(List<Repository> repositories) {
        int score = 0;
        Set<String> languages = new HashSet<>();
        LocalDate currentDate = LocalDate.now();
        List<Integer> individualScores = new ArrayList<>();

        for (Repository repo : repositories) {
            int repoScore = 0;

            // 判断是否是原创仓库
            if (!repo.isFork()) {
                repoScore += 10;
            }

            // 判断仓库活跃度
            if (repo.getUpdatedAt() != null) {
                long daysSinceUpdate = ChronoUnit.DAYS.between(repo.getUpdatedAt(), currentDate);
                if (daysSinceUpdate <= 30) {
                    repoScore += 5;
                } else if (daysSinceUpdate <= 90) {
                    repoScore += 3;
                }
            }

            // 判断语言多样性
            if (repo.getLanguage() != null) {
                languages.add(repo.getLanguage());
            }

            // 判断项目描述中的关键字（高级技术）
            if (repo.getDescription() != null) {
                if (repo.getDescription().contains("高性能") || repo.getDescription().contains("框架") ||
                        repo.getDescription().contains("分析") || repo.getDescription().contains("插件")) {
                    repoScore += 5;
                }
            }

            score += repoScore;
            individualScores.add(repoScore);
        }

        // 语言多样性评分
        score += languages.size() * 2;

        // 计算置信度（基于评分的标准差）
        double confidence = calculateConfidence(individualScores);

        System.out.println("开发者的技术评分: " + score);
        System.out.println("评分置信度: " + confidence);
        return score;
    }
    private static double calculateConfidence(List<Integer> scores) {
        if (scores.isEmpty()) return 0;

        double mean = scores.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = scores.stream().mapToDouble(score -> Math.pow(score - mean, 2)).sum() / scores.size();
        double standardDeviation = Math.sqrt(variance);

        // 使用评分标准差反向计算置信度 (标准差越小，置信度越高)
        double confidence = 1 / (1 + standardDeviation);
        return confidence;
    }

    public double calculatePercentageScore(Double score) {

        // 假设当前分数在一个无上限范围内，将其按置信度加权转换为百分制
//        confidenceLevel = redisTemplate.opsForValue().get("confidenceLevel");
        if (confidenceLevel == 0) {
            confidenceLevel = 0.5;
            redisTemplate.opsForValue().set("confidenceLevel", 0.5);
        }
        double weightedScore = score * confidenceLevel;

        // 将分数缩放到百分制 (假设最高可能分数1000为100分)
        double percentageScore = Math.min((weightedScore / 500) * 100, 100);

        // 更新置信度，假设每次计算置信度略微增加（模拟模型对分数越来越自信）
        updateConfidenceLevel();

        return percentageScore;
    }

    // 更新置信度方法
    private void updateConfidenceLevel() {
        // 这里可以根据你的需要动态调整置信度，例如每次增加固定比例（0.01表示1%）
        confidenceLevel = Math.min(this.confidenceLevel + 0.01, 1.0); // 保证不超过1
        redisTemplate.opsForValue().set("confidenceLevel", this.confidenceLevel);
    }
}
