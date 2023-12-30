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
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlElementDecl;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.stream.StreamSource;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import sk.antons.jaul.xml.XmlFormat;
import sk.antons.sbsampler.flow.FileSystem;

/**
 * Dummy implementiomn - just store input xmls to filesustem (then will be processed by integration flow)
 * @author antons
 */
@Endpoint
public class BookEndpoint {
    private static Logger log = LoggerFactory.getLogger(BookEndpoint.class);

	private static final String NAMESPACE_URI = "http://sample.antons.sk/book/1.0";

    @Autowired FileSystem fs;

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateBookRequest")
	public @ResponsePayload UpdateResponse updateBook(@RequestPayload UpdateBookRequest request) {
        UpdateResponse response = new UpdateResponse();
        response.setCode("1");
        response.setNote("file saved");
        try {
            if(request == null) throw new IllegalArgumentException("no data in request");
            if(request.getBook() == null) throw new IllegalArgumentException("no data in request");
            String xml = MarshallHelper.marshal(request);

            // just dummy action for formatting xml
            xml = XmlFormat.instance(xml, 0).indent("  ").format();

            // store xml to file
            String result = fs.storeContent(xml);

            response.setNote("file saved to " + result);

        } catch(Exception e) {
            response.setCode("1000");
            response.setNote("unable to save file " + e);
        }
		return response;
	}

	@PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateAuthorRequest")
	public @ResponsePayload UpdateResponse updateAuthor(@RequestPayload UpdateAuthorRequest request) {
        UpdateResponse response = new UpdateResponse();
        response.setCode("1");
        response.setNote("file saved");
        try {
            if(request == null) throw new IllegalArgumentException("no data in request");
            if(request.getAuthor() == null) throw new IllegalArgumentException("no data in request");
            String xml = MarshallHelper.marshal(request);

            // just dummy action for formatng xml
            xml = XmlFormat.instance(xml, 0).indent("  ").format();

            // store xml to file
            String result = fs.storeContent(xml);

            response.setNote("file saved to " + result);

        } catch(Exception e) {
            response.setCode("1000");
            response.setNote("unable to save file " + e);
        }
		return response;
	}



    public static void main(String[] argv) {
        UpdateAuthorRequest auth = new UpdateAuthorRequest();
        System.out.println(" --- " + MarshallHelper.marshal(auth));
    }

}
