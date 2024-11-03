package com.yehuo.githubdatabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchConditionDto {

    private String Country;

    private String Language;

    private Long PageSize;

    private Long PageNum;
}
