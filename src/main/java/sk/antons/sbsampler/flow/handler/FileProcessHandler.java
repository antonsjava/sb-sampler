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
package sk.antons.sbsampler.flow.handler;

import java.io.File;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.FileHeaders;
import sk.antons.jaul.Is;
import sk.antons.jaul.util.AsRuntimeEx;
import sk.antons.jaul.util.TextFile;
import sk.antons.sbsampler.map.BookMapper;
import sk.antons.sbsampler.model.Author;
import sk.antons.sbsampler.model.Book;
import sk.antons.sbsampler.rest.book.AuthorRestClient;
import sk.antons.sbsampler.rest.book.BookRestClient;
import sk.antons.sbsampler.ws.book.MarshallHelper;
import sk.antons.sbsampler.ws.book.UpdateAuthorRequest;
import sk.antons.sbsampler.ws.book.UpdateBookRequest;
import sk.antons.siutils.handler.MessageConsumer;

public class FileProcessHandler extends MessageConsumer {
    private static Logger log = LoggerFactory.getLogger(FileProcessHandler.class);

    AuthorRestClient authors;
    BookRestClient books;
    BookMapper mapper;

    public FileProcessHandler(AuthorRestClient authors, BookRestClient books, BookMapper mapper) {
        this.authors = authors;
        this.books = books;
        this.mapper = mapper;
    }

    public static FileProcessHandler of(AuthorRestClient authors, BookRestClient books, BookMapper mapper) { return new FileProcessHandler(authors, books, mapper); }

    @Override
    protected void accept(Message<?> message) throws MessagingException {
        Throwable t = null;
        File file = null;
        file = (File)message.getHeaders().get(FileHeaders.ORIGINAL_FILE);

        if(file != null) {
            if(file.exists()) {
                try {
                    String xml = TextFile.read(file.getAbsolutePath(), "utf-8");
                    Object o = MarshallHelper.unmarshal(xml);
                    if(o == null) {
                        log.error("file {} processing failed - unable to unmarshall", file.getName());
                    } else if(o instanceof UpdateAuthorRequest) {
                        UpdateAuthorRequest r = (UpdateAuthorRequest)o;
                        Author author = mapper.toAuthor(r.getAuthor());
                        if(Is.empty(author.getId())) {
                            authors.create(author);
                        } else {
                            authors.update(author);
                        }
                    } else if(o instanceof UpdateBookRequest) {
                        UpdateBookRequest r = (UpdateBookRequest)o;
                        Book book = mapper.toBook(r.getBook());
                        book.setAuthor(Author.idOnly(r.getBook().getAuthor()));
                        if(Is.empty(book.getId())) {
                            books.create(book);
                        } else {
                            books.update(book);
                        }
                    }
                } catch(Exception e) {
                    log.error("file {} processing failed {}", file.getName(), e.toString());
                    throw AsRuntimeEx.argument(e);
                }
                log.error("file {} processing done", file.getName());
            }
        }
    }

}
