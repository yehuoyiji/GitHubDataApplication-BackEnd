package com.yehuo.githubdatabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchConditionDto implements Serializable {

    private String Country;

    private String Language;

    private Long PageSize;

    private Long PageNum;
}
