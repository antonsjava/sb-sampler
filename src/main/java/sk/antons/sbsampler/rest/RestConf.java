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

package sk.antons.sbsampler.rest;

import jakarta.servlet.Filter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.antons.servlet.filter.LogFilter;
/**
 *
 * @author antons
 */
@Configuration
@ComponentScan("sk.antons.sbsampler")
public class RestConf {
    private static Logger log = LoggerFactory.getLogger("message.rest.server");


    @Bean
    @Primary
    public Filter requestResponseDumpFilter() {


        LogFilter filter = LogFilter.builder()
            .defaultConf()
                .messageConsumer(message -> log.info(message))
                .messageConsumerEnabled(() -> log.isInfoEnabled())
                .done()
            .inCase() // ignore actuator
                .request()
                    .path().startsWith("/actuator/")
                    .done()
                .conf()
                    .doNothing(true)
                    .done()
                .done()
            .inCase() // log soap messages
                .request()
                    .path().startsWith("/ws/")
                    .done()
                .conf()
                    .requestPayloadFormatter(LogFilter.Body.Xml.instance().forceOneLine(true).cutStringLiterals(200).format())
                    .responsePayloadFormatter(LogFilter.Body.Xml.instance().forceOneLine(true).cutStringLiterals(200).format())
                    .done()
                .done()
            .inCase() // log as jsons
                .request()
                    .path().startsWith("/rest/")
                    .done()
                .conf()
                    .requestPayloadFormatter(LogFilter.Body.Json.instance().forceOneLine(true).cutStringLiterals(200).format())
                    .responsePayloadFormatter(LogFilter.Body.Json.instance().forceOneLine(true).cutStringLiterals(200).format())
                    .done()
                .done()
            .inCase() // all other must be logged without payload and headers
                .request()
                    .any()
                    .done()
                .conf()
                    .requestStartPrefix("REQX") // if you see this mayby is something wrong
                    .done()
                .done()
            .build();

        log.info("rest conf: {}", filter.configurationInfo());

        return filter;

    }

}
