package com.yehuo.githubdatabackend;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
@EnableEncryptableProperties
@SpringBootApplication
public class GithubDataBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(GithubDataBackendApplication.class, args);
    }

}
