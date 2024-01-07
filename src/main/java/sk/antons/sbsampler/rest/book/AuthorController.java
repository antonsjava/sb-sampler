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
import sk.antons.sbsampler.model.Author;
import sk.antons.sbsampler.service.BookService;

/**
 * Author manipulation api. Allows to create modify and delete authors.
 *
 * @author antons
 */
@RestController
@RequestMapping(path="/rest")
public class AuthorController {
    private static Logger log = LoggerFactory.getLogger(AuthorController.class);

    @Autowired BookService service;

    /**
     * Searches authors by name.
     * @param name name substring
     * @return matched authors
     */
    @GetMapping(path="/author"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody List<Author> search(
                @RequestParam(required = false) String name
    ) {
         return service.authorSearch(name);
    }

    /**
     * Reads author by id.
     * @param id id od author
     * @return authord data
     */
    @GetMapping(path="/author/{id}"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody Author read(@PathVariable String id) {
        return service.authorRead(id);
    }

    /**
     * Create new author. Id myust be empty and name must be distinct.
     * @param author author data
     * @return id of new created author
     */
    @PostMapping(path="/author"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody String create(@RequestBody Author author) {
        return service.authorCreate(author);
    }

    /**
     * Modify existing author. Id must not be emptu
     * @param author authors data
     */
    @PutMapping(path="/author"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void update(@RequestBody Author author) {
        service.authorUpdate(author);
    }

    /**
     * Deletes authro by id.
     * @param id id od author to be deleted
     */
    @DeleteMapping(path="/author/{id}"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public void delete(@PathVariable String id) {
        service.authorDelete(id);
    }


}
