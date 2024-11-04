package com.yehuo.githubdatabackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor

public class PersonVo implements Serializable {

    String login;

    Long id;

    String node_id;

    String avatar_url;

    String gravatar_id;

    String url;

    String html_url;

    String followers_url;

    String following_url;

    String gists_url;

    String starred_url;

    String subscriptions_url;

    String organizations_url;

    String repos_url;

    String events_url;

    String received_events_url;

    String type;

    String user_view_type;

    boolean site_admin;

    String name;

    String company;

    String blog;

    String location;

    String email;

    String hireable;

    String bio;

    String twitter_username;

    Integer public_repos;

    Integer public_gists;

    Integer followers;

    Integer following;

    String created_at;

    String updated_dt;
}
