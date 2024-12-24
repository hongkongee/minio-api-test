package com.minio.minio_api_test.config;

import io.minio.MinioClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Bean
    public MinioClient minioClient() {
        // MinIO 클라이언트 설정
        return MinioClient.builder()
                .endpoint("http://10.10.30.243:30900") // MinIO 서버 URL
                .credentials("minioadmin", "minioadmin") // 인증 정보
                .build();
    }

}