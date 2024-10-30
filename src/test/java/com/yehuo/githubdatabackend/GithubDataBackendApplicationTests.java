package com.yehuo.githubdatabackend;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.util.text.AES256TextEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GithubDataBackendApplicationTests {

    @Autowired
    private StringEncryptor stringEncryptor;

    @Test
    void contextLoads() {

    }
}
