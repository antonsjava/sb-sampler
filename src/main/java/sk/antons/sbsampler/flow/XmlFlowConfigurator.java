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
package sk.antons.sbsampler.flow;

import java.io.File;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

import java.time.Duration;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.file.filters.LastModifiedFileListFilter;
import sk.antons.siutils.file.AcceptOnceFileReleaser;
import sk.antons.siutils.handler.DevNullHandler;
import sk.antons.siutils.log.SlfHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.filters.ChainFileListFilter;
import sk.antons.sbsampler.flow.handler.FileBackupHandler;
import sk.antons.sbsampler.flow.handler.FileFailHandler;
import sk.antons.sbsampler.flow.handler.FileProcessHandler;
import sk.antons.sbsampler.map.BookMapper;
import sk.antons.sbsampler.rest.book.AuthorRestClient;
import sk.antons.sbsampler.rest.book.BookRestClient;
import sk.antons.siutils.core.InactivityDelayMessageSource;
import sk.antons.siutils.core.MessageSourceBatchAction;
import sk.antons.siutils.core.OnOffMessageSource;
import sk.antons.siutils.file.RealHeadDirectoryScanner;
import sk.antons.siutils.handler.AdhocMessageConsumer;

/**
 * Dummy flow which reads xml request files from specified folder and call rest service then.
 */
@Configuration
public class XmlFlowConfigurator {
    private static Logger log = LoggerFactory.getLogger(XmlFlowConfigurator.class);


    @Autowired FileSystem fs;
    @Autowired AuthorRestClient authors;
    @Autowired BookRestClient books;
    @Autowired BookMapper mapper;


    private final AcceptOnceFileListFilter<File> xmlAcceptOnceFilter = new AcceptOnceFileListFilter<>(1000);
    private final Counter activeXmlCounter = Counter.instance();


    @Bean
    public IntegrationFlow xmlFileReadingFlow() {
        log.info("Creating file reading flow for directory " + fs.inputRoot());
        // @formatter:off
        int counter = 1;
        return IntegrationFlow
                // read XML files from processing directory
                .from(fileReadingMessageSource(), e -> e  // cita vstupny adresar a hlada xmlka
                    .autoStartup(true)
                    .poller(Pollers
                        .fixedDelay(Duration.ofSeconds(1))
                        .maxMessagesPerPoll(1)
                        )
                    )
                //.handle(SlfHandler.of(lg -> lg.debug(xlog, "xml file {} processing start ", lg.header(FileHeaders.FILENAME))))
                .enrichHeaders(Map.of("errorChannel", "xmlFileReadingErrorChannel")) // registruj flow pre padnute spracovanie
                .handle(AdhocMessageConsumer.consumeBy(m -> activeXmlCounter.increase()))  // aktivuje podmientku citania
                .handle(FileProcessHandler.of(authors, books, mapper))
                .handle(FileBackupHandler.of(fs)) // odoz spracovane xml do backup adresara
                .handle(AcceptOnceFileReleaser.of(xmlAcceptOnceFilter))       // uvolni file z accept once filtra
                .handle(AdhocMessageConsumer.consumeBy(m -> activeXmlCounter.decrease()))
                //.handle(SlfHandler.of(lg -> lg.debug(xlog, "xml file {} processing done", lg.header(FileHeaders.FILENAME))))
                .handle(DevNullHandler.of())                                  // ukonci flow
                .get();
        // @formatter:on
    }

    // error flow pre spracovanie xml
    @Bean
    public IntegrationFlow xmlFileReadingErrorFlow() {
        return IntegrationFlow
                .from("xmlFileReadingErrorChannel")
                .handle(FileFailHandler.of(fs)) // odoz nespracovane xml do karantenneho adresara
                .handle(SlfHandler.of(lg -> lg.error(log, "processing failed for {} {}", lg.header(), lg.payload())))
                .handle(AdhocMessageConsumer.consumeBy(m -> activeXmlCounter.decrease()))
                .get();
    }

    private MessageSource<File> fileReadingMessageSource() {

        SimplePatternFileListFilter patternFilter = new SimplePatternFileListFilter("*.xml");
        LastModifiedFileListFilter lastModifiedFilter = new LastModifiedFileListFilter(10);
        ChainFileListFilter<File> compositeFilter = new ChainFileListFilter<>(
                List.of(patternFilter, lastModifiedFilter, xmlAcceptOnceFilter));

        RealHeadDirectoryScanner scanner = new RealHeadDirectoryScanner(1);
        scanner.setFilter(compositeFilter);

        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(new File(fs.inputRoot()));
        source.setAutoCreateDirectory(true);
        source.setScanner(scanner);
        //source.setUseWatchService(true); // very important
        //return source;
        return MessageSourceBatchAction.of(
                        OnOffMessageSource.of(
                                 InactivityDelayMessageSource.of(source)
                                     .inactivityDelay(Duration.ofMinutes(1))
                        ).condition(() -> activeXmlCounter.count() < 1)
                ).async(true)
                .action(() -> {
                        log.debug("stop processing");
                     })
                .after();
    }


    private static class Counter {
        private int count = 0;
        public synchronized void increase() { count++; }
        public synchronized void decrease() { count--; }
        public synchronized int count() { return count; }
        public static Counter instance() { return new Counter(); }
    }


}
