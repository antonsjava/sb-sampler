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

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author antons
 */
public class MarshallHelper {
    private static Logger log = LoggerFactory.getLogger(MarshallHelper.class);

    private static JAXBContext jaxbCtx = null;
    public static JAXBContext jaxbCtx() {
        if(jaxbCtx == null) {
            try {
                jaxbCtx = JAXBContext.newInstance(
            "sk.antons.sbsampler.ws.book"
                );
            } catch (Exception e) {
                log.error("Unable to create jaxb context ", e);
            }
        }
        return jaxbCtx;
    }

    public static Object unmarshal(String xml) {
        if(xml == null) return null;
        try {
            Unmarshaller um = jaxbCtx().createUnmarshaller();
            Object obj = um.unmarshal(new StreamSource(new StringReader(xml)));
            return obj;
        } catch (JAXBException e) {
            log.error("Unable to unmarshal xml '{}' {}", xml, e.toString());
            throw new IllegalStateException("Unable to unmarshal the data ");
        }
    }

    public static String marshal(Object scbMessage) {
        if (scbMessage == null) return null;
        try {
            Marshaller um = jaxbCtx().createMarshaller();
            try {
                StringWriter sw = new StringWriter();
                um.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
                um.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
                um.marshal(scbMessage, sw);
                return sw.toString();
            } catch (JAXBException e) {
                log.error("Unable to marshal message {} {}", scbMessage, e.toString());
                throw new IllegalStateException("Unable to marshal the data " + scbMessage.getClass());
            }
        } catch (JAXBException e) {
            log.error("Error occured while initializing JAXB context {}", e.toString());
            throw new IllegalStateException("Unable to marshal the data " + scbMessage.getClass());
        }
    }

}
