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
package sk.antons.sbsampler.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import sk.antons.jaul.Is;
import sk.antons.sbsampler.fail.ValidationException;
import sk.antons.sbsampler.model.Author;
import sk.antons.sbsampler.model.Book;
import sk.antons.sbsampler.repo.BookRepo;

/**
 *
 * @author antons
 */
@Service
public class BookService {

    @Autowired BookRepo repo;

    public List<Book> bookSearch(String title, String authorId) {
        if(Is.empty(title) && Is.empty(authorId)) throw ValidationException.mandatory("title/authorId");
        return repo.findBook(title, authorId);
    }

    public Book bookRead(String id) {
        if(Is.empty(id)) throw ValidationException.mandatory("id");
        return repo.readBook(id);
    }

    public String bookCreate(Book book) {
        if(Is.empty(book)) throw ValidationException.mandatory("book");
        if(!Is.empty(book.getId())) throw ValidationException.format("id", book.getId());
        if(Is.empty(book.getTitle())) throw ValidationException.mandatory("book.title");
        if(Is.empty(book.getAuthor())) throw ValidationException.mandatory("book.author");
        String authorId = book.getAuthor().getId();
        if(Is.empty(authorId)) {
            authorId = authorCreate(book.getAuthor());
        }
        return repo.createBook(book.getTitle(), book.getAbstractText(), authorId);
    }

    public void bookUpdate(Book book) {
        if(Is.empty(book)) throw ValidationException.mandatory("book");
        if(Is.empty(book.getId())) throw ValidationException.mandatory("book.id");
        if(Is.empty(book.getTitle())) throw ValidationException.mandatory("book.title");
        if(Is.empty(book.getAuthor())) throw ValidationException.mandatory("book.author");
        String authorId = book.getAuthor().getId();
        if(Is.empty(authorId)) {
            authorId = authorCreate(book.getAuthor());
        }
        repo.updateBook(book.getId(), book.getTitle(), book.getAbstractText(), authorId);
    }

    public void bookDelete(@PathVariable String id) {
        if(Is.empty(id)) throw ValidationException.mandatory("id");
        repo.deleteBook(id);
    }



    public List<Author> authorSearch(String name) {
        if(Is.empty(name)) throw ValidationException.mandatory("name");
        return repo.findAuthor(name);
    }

    public Author authorRead(String id) {
        if(Is.empty(id)) throw ValidationException.mandatory("id");
        return repo.readAuthor(id);
    }

    public String authorCreate(Author author) {
        if(Is.empty(author)) throw ValidationException.mandatory("author");
        if(!Is.empty(author.getId())) throw ValidationException.format("id", author.getId());
        if(Is.empty(author.getName())) throw ValidationException.mandatory("author.name");
        return repo.createAuthor(author.getName());
    }

    public void authorUpdate(Author author) {
        if(Is.empty(author)) throw ValidationException.mandatory("author");
        if(Is.empty(author.getId())) throw ValidationException.mandatory("author.id");
        if(Is.empty(author.getName())) throw ValidationException.mandatory("author.name");
        repo.updateAuthor(author.getId(), author.getName());
    }

    public void authorDelete(@PathVariable String id) {
        if(Is.empty(id)) throw ValidationException.mandatory("id");
        repo.deleteAuthor(id);
    }

}
