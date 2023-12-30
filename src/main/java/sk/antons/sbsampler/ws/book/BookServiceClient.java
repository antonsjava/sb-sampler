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
package sk.antons.sbsampler.ws.book;

import jakarta.xml.soap.MessageFactory;
import jakarta.xml.soap.SOAPConstants;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.client.core.support.WebServiceGatewaySupport;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HttpContext;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;
import org.springframework.ws.transport.http.HttpComponentsMessageSender;
import org.w3c.dom.Element;
import sk.antons.jaul.pojo.Pojo;
import sk.antons.sbutils.ws.SBWSLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * minimalistic ws client - just to show log interceptor
 * @author antons
 */
public class BookServiceClient extends WebServiceGatewaySupport {
    private static Logger log = LoggerFactory.getLogger("message.ws.client");

    private String endpoint;
    private boolean dump;

    public BookServiceClient(String endpoint, boolean dump) {
        this.endpoint = endpoint;
        this.dump = dump;
        setWebServiceTemplate(webServiceTemplate());
    }
    public static BookServiceClient instance(String endpoint, boolean dump) { return new BookServiceClient(endpoint, dump); }

    public String marshall(Object o) {
        if(o == null) return "";
        Marshaller marshaller = getMarshaller();
        if (marshaller == null) {
            throw new IllegalStateException("No marshaller registered. Check configuration of WebServiceTemplate.");
        }
        StringWriter sw = new StringWriter();
        Result result = new StreamResult(sw);
        try {
            marshaller.marshal(o, result);
            return sw.toString();
        } catch (XmlMappingException | IOException e) {
            log.info(e.getMessage(), e);
            throw new IllegalArgumentException("unable to serialize " + o, e);
        }
    }

    public Object unmarshall(String xml) {
        if(xml == null) return null;
        Unmarshaller unmarshaller = getUnmarshaller();
        if (unmarshaller == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
        try {
            StreamSource source = new StreamSource(new StringReader(xml));
            return unmarshaller.unmarshal(source);
        } catch (XmlMappingException | IOException e) {
            log.info(e.getMessage(), e);
            throw new IllegalStateException("Unable to parse " + xml);
        }
    }

    public Object unmarshall(Element element) {
        if(element == null) return null;
        Unmarshaller unmarshaller = getUnmarshaller();
        if (unmarshaller == null) {
            throw new IllegalStateException("No unmarshaller registered. Check configuration of WebServiceTemplate.");
        }
        try {
            DOMSource source = new DOMSource(element);
            return unmarshaller.unmarshal(source);
        } catch (XmlMappingException | IOException e) {
            log.info(e.getMessage(), e);
            throw new IllegalStateException("Unable to parse " + element);
        }
    }

    private Jaxb2Marshaller marshaller() {
        String[] jaxbPaths = new String[] {
            "sk.antons.sbsampler.ws.book"
        };
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setContextPaths(jaxbPaths);
        return marshaller;
    }

    private HttpComponentsMessageSender csruHttpComponentsMessageSender() {
        HttpComponentsMessageSender httpComponentsMessageSender = new HttpComponentsMessageSender() {

            HttpClientContext context;

            @Override
            public void afterPropertiesSet() throws Exception {
                super.afterPropertiesSet();
                HttpHost[] hosts = new HttpHost[] {
                    URIUtils.extractHost(new URI(endpoint)),
                };

                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();

                // Add AuthCache to the execution context
                HttpClientContext context = HttpClientContext.create();
                context.setCredentialsProvider(credsProvider);
                context.setAuthCache(authCache);
                this.context = context;
            }


            @Override
            protected HttpContext createContext(URI uri) {
                return context;
            }
        };
        // set the basic authorization credentials
        //httpComponentsMessageSender.setCredentials(sredentials);

        httpComponentsMessageSender.setHttpClient(apacheHttpClient(120));
        return httpComponentsMessageSender;
    }

    private static org.apache.http.client.HttpClient apacheHttpClient(int timeoutInSeconds) {

        org.apache.http.client.HttpClient client = HttpClients.custom()
            //.setConnectionManager(connManager)
            //.setSSLSocketFactory(sslConnectionFactory)
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(1000*10).setSocketTimeout(1000*timeoutInSeconds).build())
            .addInterceptorFirst(new HttpComponentsMessageSender.RemoveSoapHeadersInterceptor())
            .build();
        return client;
    }

    private WebServiceTemplate webServiceTemplate() {
        try {
            HttpComponentsMessageSender csruHttpComponentsMessageSender = csruHttpComponentsMessageSender();
            Jaxb2Marshaller marshaller = marshaller();
            WebServiceTemplate webServiceTemplate = new WebServiceTemplate();
            webServiceTemplate.setMarshaller(marshaller);
            webServiceTemplate.setUnmarshaller(marshaller);
            // set a HttpComponentsMessageSender which provides support for basic
            // authentication
            webServiceTemplate.setMessageSender(csruHttpComponentsMessageSender);

            //MessageFactory msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            MessageFactory msgFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);
            SaajSoapMessageFactory newSoapMessageFactory = new SaajSoapMessageFactory(msgFactory);
            webServiceTemplate.setMessageFactory(newSoapMessageFactory);

            // uncomment for logging of requests/responses to CSRU
            if(dump) {
                ClientInterceptor[] interceptors = new ClientInterceptor[]{
                    SBWSLoggingInterceptor.instance()
                        .logger(m -> log.info(m))
                        .loggerEnabled(() -> log.isInfoEnabled())
                };
                webServiceTemplate.setInterceptors(interceptors);
            }


            return webServiceTemplate;
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }


    public UpdateResponse updateBook(UpdateBookRequest request) {

        UpdateResponse response = (UpdateResponse) getWebServiceTemplate().marshalSendAndReceive(endpoint, request);
        return response;

    }

    public UpdateResponse updateAuthor(UpdateAuthorRequest request) {

        UpdateResponse response = (UpdateResponse) getWebServiceTemplate().marshalSendAndReceive(endpoint, request);
        return response;

    }

    public static void main(String[] argv) {
        BookServiceClient client = BookServiceClient.instance("http://localhost:8080/ws/book", true);
        System.out.println(" start update");
        UpdateBookRequest request = new UpdateBookRequest();
        request.setBook(new WsBook());
        request.getBook().setTitle("pokus");
        request.getBook().setAuthor("t1");
        request.getBook().setAbstract("something long");
        UpdateResponse response = client.updateBook(request);
        System.out.println(" update done " + Pojo.dumper(true).json(response));
    }
}
