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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.file.FileHeaders;
import sk.antons.sbsampler.flow.FileSystem;
import sk.antons.siutils.handler.MessageConsumer;

public class FileBackupHandler extends MessageConsumer {
    private static Logger log = LoggerFactory.getLogger(FileBackupHandler.class);

    FileSystem fs;

    public FileBackupHandler(FileSystem fs) {
        this.fs = fs;
    }

    public static FileBackupHandler of(FileSystem fs) { return new FileBackupHandler(fs); }

    @Override
    protected void accept(Message<?> message) throws MessagingException {
        Throwable t = null;
        File file = null;
        file = (File)message.getHeaders().get(FileHeaders.ORIGINAL_FILE);

        if(file != null) {
            if(file.exists()) {
                File destination = new File(fs.backupRoot()+ "/" + file.getName());
                try {
                    Files.move(file.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch(Exception e) {
                    log.error("file {} processing failed {}", file.getName(), e.toString());
                }
                log.error("file {} processing done {}", file.getName());
            }
        }
    }

}
