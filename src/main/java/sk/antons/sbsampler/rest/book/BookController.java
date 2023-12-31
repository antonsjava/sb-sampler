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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sk.antons.sbsampler.model.Book;
import sk.antons.sbsampler.service.BookService;

@RestController
@RequestMapping(path="/rest")
public class BookController {
    private static Logger log = LoggerFactory.getLogger(BookController.class);

    @Autowired BookService service;

    @GetMapping(path="/book"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody List<Book> search(
                @RequestParam(required = false) String title
                , @RequestParam(required = false) String authorId
    ) {
         return service.bookSearch(title, authorId);
    }

    @GetMapping(path="/book/{id}"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody Book read(@PathVariable String id) {
        return service.bookRead(id);
    }

    @PostMapping(path="/book"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody String create(@RequestBody Book book) {
        return service.bookCreate(book);
    }

    @PutMapping(path="/book"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void update(@RequestBody Book book) {
        service.bookUpdate(book);
    }

    @DeleteMapping(path="/book/{id}"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void delete(@PathVariable String id) {
        service.bookDelete(id);
    }


}
