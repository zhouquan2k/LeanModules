package io.leanddd.module.file.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import java.net.URI;

/**
 * S3客户端配置
 */
@Configuration
public class S3Config {

    @Value("${app.file.s3.endpoint:http://localhost:8333}")
    private String endpoint;

    @Value("${app.file.s3.region:us-east-1}")
    private String region;

    @Value("${app.file.s3.accessKey:}")
    private String accessKey;

    @Value("${app.file.s3.secretKey:}")
    private String secretKey;

    @Value("${app.file.s3.pathStyleAccessEnabled:true}")
    private boolean pathStyleAccessEnabled;

    @Bean
    public S3Client s3Client() {
        S3ClientBuilder builder = S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .forcePathStyle(pathStyleAccessEnabled);

        // 如果配置了访问密钥，则使用静态凭证
        if (accessKey != null && !accessKey.isEmpty() && 
            secretKey != null && !secretKey.isEmpty()) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            builder.credentialsProvider(StaticCredentialsProvider.create(credentials));
        }

        return builder.build();
    }
}
