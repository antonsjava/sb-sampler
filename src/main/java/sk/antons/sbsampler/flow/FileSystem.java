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


import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import sk.antons.jaul.util.AsRuntimeEx;
import sk.antons.jaul.util.TextFile;

/**
 * Just hadle folders for flow
 * @author antons
 */
@Component
@Slf4j
public class FileSystem {


    @Value("${filesystem.input.dir:./target/fs/input}")
    private String inputRoot;

    @Value("${filesystem.backup.dir:./target/fs/backup}")
    private String backupRoot;

    @Value("${filesystem.fail.dir:./target/fs/faul}")
    private String failRoot;

    public String inputRoot() { return inputRoot; }
    public String backupRoot() { return backupRoot; }
    public String failRoot() { return failRoot; }


    @PostConstruct
    public void init() {
        ensurePath(inputRoot);
        ensurePath(backupRoot);
        ensurePath(failRoot);
    }

    private void ensurePath(String path) {
        if(path == null) return ;
        File f = new File(path);
        if(!f.exists()) {
            log.info("path {} creating", path);
            boolean rv = f.mkdirs();
            log.info("path {} created {}", path, rv);
        }
    }

    private static long counter = System.currentTimeMillis();
    public String storeContent(String xml) {
        String filename = (counter++) + "-" + LocalDate.now().format(DateTimeFormatter.ISO_DATE) + ".xml";
        TextFile.save(inputRoot() + "/" + filename, "utf-8", xml);
        return filename;
    }


    public synchronized boolean isInputEmpty() {
        return isFolderEmpty(inputRoot, ".xml");
    }

    private static boolean isFolderEmpty(String dir, String endsWith) {
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            Optional<Path> result = stream
              .filter(file -> !Files.isDirectory(file))
              .filter(file -> file.getFileName().toString().endsWith(endsWith))
              .findFirst();
            return !result.isPresent();

        } catch (IOException e) {
            throw AsRuntimeEx.argument(e, "unable to list files from {}", dir);
        }
    }


}
