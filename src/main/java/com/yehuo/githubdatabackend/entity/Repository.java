package com.yehuo.githubdatabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Repository implements Serializable {

    // 仓库名
    private String name;

    // 仓库描述
    private String description;

    // 仓库地址
    private String url;

    // star数
    private String stargazers_count;

    // 仓库主语言
    private String language;

    // 主题
    private List<String> topics;

    // 创建时间
    LocalDate createdAt;

    // 更新时间
    LocalDate updatedAt;

    // 是否fork
    boolean isFork;

    // 最终分数
    Double score;
}
