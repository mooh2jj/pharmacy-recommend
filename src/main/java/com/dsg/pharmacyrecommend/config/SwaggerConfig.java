package com.dsg.pharmacyrecommend.config;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local", "dev"}) // local, dev 프로파일에서만 Swagger 활성화 외부에서 api 노출하면 안되니깐, prod는 안됨!
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .openapi("3.0.0")  // openAPI 버전 명시
                .components(new Components())
                .info(apiInfo());
    }

    private Info apiInfo() {
        String projectName = "Pharmacy Recommendation Service";
        return new Info()
                .title(projectName + " Swagger")
                .description(projectName + " REST API Swagger Documentation")
                .version("1.0.0");
    }


}
