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

package sk.antons.sbsampler.repo;

import static com.fasterxml.jackson.databind.util.ClassUtil.name;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.antons.jaul.Is;
import sk.antons.jaul.util.AsRuntimeEx;
import sk.antons.jaul.util.Resource;
import sk.antons.jaul.util.TextFile;
import sk.antons.jdbc.ds.LogDataSource;
import sk.antons.jdbc.log.LogConfig;
import sk.antons.jdbc.util.Db;
import sk.antons.jdbc.util.Script;
import sk.antons.sbsampler.fail.ResourceNotFoundException;
import sk.antons.sbsampler.model.Author;
import sk.antons.sbsampler.model.Book;

/**
 *
 * @author antons
 */
public class BookRepo {
    private static Logger log = LoggerFactory.getLogger(BookRepo.class);

    DataSource ds;

    public BookRepo(DataSource ds, boolean dump) {
        if(dump) {

	        ds = LogDataSource.wrap(ds,
		    	LogConfig.instance(
					() -> log.isInfoEnabled()
					, (message) -> log.info(message))
                    .transaction(true)
                    .statement(true)
                    .resultSet(true)
		        );
        }

        this.ds = ds;
        init();
    }

    private static long counter = System.currentTimeMillis();
    private static synchronized String nextId() { return "" + counter++; }

    public String createAuthor(String name) {
        try (Db db = Db.instance(ds)) {

            db.prepareStatement("select id from authors where name = ?");
            db.ps().setString(1, name);
            ResultSet rs = db.executeQuery();
            if(rs.next()) throw RepoException.instance("author with name "+ name+" already exists");

            String id = nextId();
            db.prepareStatement("insert into authors (id, name) values (?, ?)");
            db.ps().setString(1, id);
            db.ps().setString(2, name);
            int rows = db.executeUpdate();
            if(rows != 1) throw RepoException.instance("unable to insert author "+ name);
            db.conn().commit();
            return id;

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }

    public void updateAuthor(String id, String name) {
        try (Db db = Db.instance(ds)) {

            Author author = readAuthorRaw(db, id);
            if(author.getName().equals(name)) return ;

            db.prepareStatement("select id from authors where name = ?");
            db.ps().setString(1, name);
            ResultSet rs = db.executeQuery();
            if(rs.next()) throw RepoException.instance("author with name "+ name+" already exists");

            db.prepareStatement("update table authors set name=? where id=?");
            db.ps().setString(1, name);
            db.ps().setString(2, id);
            int rows = db.executeUpdate();
            if(rows != 1) throw RepoException.instance("unable to update author "+ id);
            db.conn().commit();

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }

    public Author readAuthor(String id) {
        try (Db db = Db.instance(ds)) {

            return readAuthorRaw(db, id);

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }
    private Author readAuthorRaw(Db db, String id) throws SQLException {

        db.prepareStatement("select name from authors where id = ?");
        db.ps().setString(1, id);
        ResultSet rs = db.executeQuery();
        if(rs.next()) {
            int row = 1;
            Author author = new Author();
            author.setId(id);
            author.setName(rs.getString(row++));
            return author;
        } else {
            throw ResourceNotFoundException.instance("author", id);
        }

    }

    private static String escapeLike(String value) {
        value = value
            .replace("!", "!!")
            .replace("%", "!%")
            .replace("_", "!_")
            .replace("[", "![");
        return value;
    }

    public List<Author> findAuthor(String name) {
        List<Author> list = new ArrayList<>();
        try (Db db = Db.instance(ds)) {

            db.prepareStatement("select id, name from authors where name like ?");
            db.ps().setString(1, '%' + escapeLike(name) + '%');
            ResultSet rs = db.executeQuery();
            while(rs.next()) {
                int row = 1;
                Author author = new Author();
                author.setId(rs.getString(row++));
                author.setName(rs.getString(row++));
                list.add(author);
            }

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
        return list;
    }

    public void deleteAuthor(String id) {
        try (Db db = Db.instance(ds)) {

            db.prepareStatement("delete table authors where id = ?");
            db.ps().setString(1, id);
            db.executeUpdate();
            db.conn().commit();

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }

    public String createBook(String title, String abstractText, String authorId) {
        try (Db db = Db.instance(ds)) {

            Author author = readAuthorRaw(db, authorId);

            String id = nextId();
            db.prepareStatement("insert into books (id, title, abstrct, author) values (?, ?, ?, ?)");
            db.ps().setString(1, id);
            db.ps().setString(2, title);
            db.ps().setString(3, abstractText);
            db.ps().setString(4, authorId);
            int rows = db.executeUpdate();
            if(rows != 1) throw RepoException.instance("unable to insert book "+ title);
            db.conn().commit();
            return id;

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }
    public void updateBook(String id, String title, String abstractText, String authorId) {
        try (Db db = Db.instance(ds)) {

            Author author = readAuthorRaw(db, id);
            Book book = readBookRaw(db, id);

            db.prepareStatement("update table books set title=?, abstrct=?, author=? where id=?");
            db.ps().setString(1, title);
            db.ps().setString(2, abstractText);
            db.ps().setString(3, authorId);
            db.ps().setString(4, id);
            int rows = db.executeUpdate();
            if(rows != 1) throw RepoException.instance("unable to update books "+ id);
            db.conn().commit();

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }

    public Book readBook(String id) {
        try (Db db = Db.instance(ds)) {

            return readBookRaw(db, id);

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }

    private Book readBookRaw(Db db, String id) throws SQLException {

        db.prepareStatement("select t1.title, t1.abstrct, t1.author, t2.name from books t1, authors t2 where t1.author = t2.id and t1.id = ?");
        db.ps().setString(1, id);
        ResultSet rs = db.executeQuery();
        if(rs.next()) {
            int row = 1;
            Book book = new Book();
            book.setId(id);
            book.setTitle(rs.getString(row++));
            book.setAbstractText(rs.getString(row++));
            book.setAuthor(new Author());
            book.getAuthor().setId(rs.getString(row++));
            book.getAuthor().setName(rs.getString(row++));
            return book;
        } else {
            throw ResourceNotFoundException.instance("book", id);
        }

    }

    public List<Book> findBook(String title, String authorId) {
        List<Book> list = new ArrayList<>();
        if(Is.empty(authorId) && Is.empty(title)) return list;
        try (Db db = Db.instance(ds)) {
            StringBuilder stm = new StringBuilder();
            stm.append("select t1.id, t1.title, t1.abstrct, t1.author, t2.name from books t1, authors t2 where t1.author = t2.id ");
            if(!Is.empty(authorId)) stm.append(" and t1.author=?");
            if(!Is.empty(title)) stm.append(" and t1.title like ?");
            db.prepareStatement(stm.toString());
            int num = 1;
            if(!Is.empty(authorId)) db.ps().setString(num++, authorId);
            if(!Is.empty(title)) db.ps().setString(num++, '%' + escapeLike(title) + '%');
            ResultSet rs = db.executeQuery();
            while(rs.next()) {
                int row = 1;
                Book book = new Book();
                book.setId(rs.getString(row++));
                book.setTitle(rs.getString(row++));
                book.setAbstractText(rs.getString(row++));
                book.setAuthor(new Author());
                book.getAuthor().setId(rs.getString(row++));
                book.getAuthor().setName(rs.getString(row++));
                list.add(book);
            }

        } catch(Exception e) {
            e.printStackTrace();
            throw AsRuntimeEx.state(e);
        }
        return list;
    }
    public void deleteBook(String id) {
        try (Db db = Db.instance(ds)) {

            db.prepareStatement("delete table books where id = ?");
            db.ps().setString(1, id);
            db.executeUpdate();
            db.conn().commit();

        } catch(Exception e) {
            throw AsRuntimeEx.state(e);
        }
    }



    private static String SCHEMA =
        """

        create table authors (id varchar(50) primary key, name varchar(5000));

        create table books (id varchar(50) primary key, title varchar(500), abstrct varchar(10000), author varchar(50));
        create index books_author ON books (author) ;

        """;

    private void init() {

        try (Db db = Db.instance(ds)) {
            db.executeQuery("select count(*) from authors");
        } catch(Exception e) {
            log.info("problem to connect database {}", e.toString());
            log.info("probably not initiated yet try to initialize", e.toString());
            try (Db db = Db.instance(ds)) {
                Script.instance(SCHEMA).execute(db.conn());

                String initdb = TextFile.read(Resource.url("classpath:db/books.sql").inputStream(), "utf-8");
                Script.instance(initdb)
                        .commitAfter(100)
                        .execute(db.conn());
            } catch(Exception ee) {
                log.info("problem to initiate database {}", ee.toString());
                throw AsRuntimeEx.state(ee);
            }
        }
    }
}
