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

import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.client.RestTemplate;
import sk.antons.jaul.pojo.Pojo;
import sk.antons.sbsampler.model.Book;
import sk.antons.sbutils.rest.RestTemplateClient;

/**
 *
 * @author antons
 */
public class BookRestClient {

    private RestTemplate template;
    private String root;

    public BookRestClient(RestTemplate template, String root) {
        this.template = template;
        this.root = root;
    }
    public static BookRestClient instance(RestTemplate template, String root) { return new BookRestClient(template, root); }

    private RestTemplateClient client;
    private RestTemplateClient client() {
        if(client == null) {
            client = RestTemplateClient.Builder.instance()
            .template(template)
            .root(root)
            .client();
        }
        return client;
    }


    public List<Book> search(String name) {
        RestTemplateClient.Path path = RestTemplateClient.Path.builder()
            .append("/book")
            .query("name", name);
        RestTemplateClient.Request request = client().get()
            .path(path.build());
        return request.call(new ParameterizedTypeReference<List<Book>>() {});
    }

    public Book read(String id) {
        RestTemplateClient.Path path = RestTemplateClient.Path.builder()
            .append("/book/")
            .pathVariable(id);
        RestTemplateClient.Request request = client().get()
            .path(path.build());
        return request.call(Book.class);
    }

    public void create(Book author) {
        RestTemplateClient.Request request = client()
            .post()
            .path("/book")
            .content(author);
        request.call();
    }

    public void update(Book author) {
        RestTemplateClient.Request request = client()
            .put()
            .path("/book")
            .content(author);
        request.call();
    }

    public void delete(String id) {
        RestTemplateClient.Path path = RestTemplateClient.Path.builder()
            .append("/book/")
            .pathVariable(id);
        RestTemplateClient.Request request = client().delete()
            .path(path.build());
        request.call();
    }


    public static void main(String[] argv) {
        BookRestClient client = BookRestClient.instance(new RestTemplate(), "http://localhost:8080/rest");
        System.out.println(" search: " + client.search("Josef"));
        System.out.println(" read: " + client.read("121"));
        client.create(Pojo.messer().junk(Book.class));
        System.out.println(" create done");
        client.update(Pojo.messer().junk(Book.class));
        System.out.println(" update done");
        client.delete("112");
        System.out.println(" delete done");
    }
}
