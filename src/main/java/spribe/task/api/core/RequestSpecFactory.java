package spribe.task.api.core;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.LogConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.filter.log.LogDetail;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spribe.task.common.env.ConfigFactoryProvider;


public final class RequestSpecFactory {
    private static final Logger log = LoggerFactory.getLogger(RequestSpecFactory.class);

    private static volatile RequestSpecification CACHED;

    private RequestSpecFactory() {
    }

    public static RequestSpecification defaultSpec() {
        if (CACHED == null) {
            synchronized (RequestSpecFactory.class) {
                if (CACHED == null) {
                    String baseUrl = ConfigFactoryProvider.app().baseUrl();
                    log.info("Initializing RequestSpecification with baseUrl={}", baseUrl);

                    RestAssured.defaultParser = Parser.JSON;

                    RestAssuredConfig raConfig = RestAssured.config()
                            .logConfig(LogConfig.logConfig()
                                    .enableLoggingOfRequestAndResponseIfValidationFails());

                    CACHED = new RequestSpecBuilder()
                            .setBaseUri(baseUrl)
                            .setContentType("application/json")
                            .setAccept("application/json")
                            .setConfig(raConfig)
                            .addFilter(new AllureRestAssured())
                            .log(LogDetail.URI)
                            .build();
                }
            }
        }
        return CACHED;
    }
}
