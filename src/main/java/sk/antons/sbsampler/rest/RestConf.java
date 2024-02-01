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
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import sk.antons.servlet.filter.LogFilter;
import sk.antons.servlet.mimic.MimicServlet;
import sk.antons.servlet.mimic.builder.ProcessorBuilder;
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
                    .or().path().startsWith("/mongo/")
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

        log.info("rest log conf: {}", filter.configurationInfo());

        return filter;

    }

    // some mock api
    @Bean
    public ServletRegistrationBean exampleServletBean() {
        MimicServlet servlet = MimicServlet.builder()
            .inCase()
                .when()
                    .path().equals("/mock/json")
                    .done()
                .process(MimicServlet.processor()
                    .content("{\"name\":\"john\", \"age\": 20}")
                    .contentType("application/json")
                    .build())
            .inCase()
                .when()
                    .path().equals("/mock/xml")
                    .done()
                .process(MimicServlet.processor()
                    .content("<person><name>john</name><age>20</age></person>")
                    .contentType("application/xml")
                    .build())
            .inCase()
                .when()
                    .path().equals("/mock/testsoap")
                    .and().method().equals("POST")
                    .and().xmlContent("Body", "Person", "Name").contains("ex")
                    .done()
                .process(MimicServlet.processor()
                    .content("<SOAP-ENV:Envelope xmlns:SOAP-ENV =\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsi =\"http://www.w3.org/1999/XMLSchema-instance\" xmlns:xsd =\"http://www.w3.org/1999/XMLSchema\"><SOAP-ENV:Body><SOAP-ENV:Fault><faultcode xsi:type =\"xsd:string\">SOAP-ENV:Client</faultcode><faultstring xsi:type =\"xsd:string\">Failed to locate method (ValidateCreditCard) in class (examplesCreditCard) at /usr/local/ActivePerl-5.6/lib/site_perl/5.6.0/SOAP/Lite.pm line 1555.</faultstring></SOAP-ENV:Fault></SOAP-ENV:Body></SOAP-ENV:Envelope>")
                    .contentType("application/soap+xml")
                    .build())
            .inCase()
                .when()
                    .path().equals("/mock/testsoap")
                    .and().method().equals("POST")
                    .done()
                .process(MimicServlet.processor()
                    .content("<SOAP-ENV:Envelope xmlns:SOAP-ENV=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ns2=\"http://www.tekelec.com/sds/\"><SOAP-ENV:Header></SOAP-ENV:Header><SOAP-ENV:Body><ns2:sdsResult affected=\"affected\" error=\"error\" description=\"description\">        </ns2:sdsResult></SOAP-ENV:Body></SOAP-ENV:Envelope>")
                    .contentType("application/soap+xml")
                    .build())
            .inCase()
                .when()
                    .path().equals("/mock/gen")
                    .done()
                .process((req, res) -> {
                    ProcessorBuilder.ProcessorHelper helper = ProcessorBuilder.ProcessorHelper.instance(req, res);
                    String param = helper.request().getParameter("name");
                    if(param == null) param = "noname";
                    helper.contentAsText("person "+param+" " + System.currentTimeMillis());
                    helper.response().setContentType("text/plain");
                    return true;
                })
            .build();
        log.info("mimic conf: {}", servlet.configurationInfo());
        ServletRegistrationBean bean = new ServletRegistrationBean(
          servlet, "/mock/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

}
