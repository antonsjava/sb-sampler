/*
 * Copyright 2023 Anton Straka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sk.antons.sbsampler.rest.book;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.antons.sbutils.http.LoggingInterceptor;

/**
 *
 * @author antons
 */
@Configuration
public class BookClientConf {
    private static Logger log = LoggerFactory.getLogger("message.rest.client");

    @Value("${rest.book.endpoint}")
    private String endpoint;
    @Value("${rest.book.dump:true}")
    private boolean dump;

    @Autowired RestTemplateBuilder templateBuilder;

    @Bean
    public AuthorRestClient authors() {
        RestTemplate template = templateBuilder
                .setConnectTimeout(Duration.ofSeconds(120))
                .build();
        if(dump) {

            LoggingInterceptor.instance()
                //.requestHeaders(LoggingInterceptor.Headers.all())
                .requestBody(LoggingInterceptor.Body.json().forceOneLine().transform())
                //.responseHeaders(LoggingInterceptor.Headers.all())
                .responseBody(LoggingInterceptor.Body.json().forceOneLine().transform())
                .loggerEnabled( () -> log.isInfoEnabled())
                .logger(m -> log.info(m))
                .addToTemplate(template);

        }
        return AuthorRestClient.instance(template, endpoint);
    }

    @Bean
    public BookRestClient books() {
        RestTemplate template = templateBuilder
                .setConnectTimeout(Duration.ofSeconds(120))
                .build();
        if(dump) {

            LoggingInterceptor.instance()
                //.requestHeaders(LoggingInterceptor.Headers.all())
                .requestBody(LoggingInterceptor.Body.json().forceOneLine().transform())
                //.responseHeaders(LoggingInterceptor.Headers.all())
                .responseBody(LoggingInterceptor.Body.json().forceOneLine().transform())
                .loggerEnabled( () -> log.isInfoEnabled())
                .logger(m -> log.info(m))
                .addToTemplate(template);

        }
        return BookRestClient.instance(template, endpoint);
    }
}
