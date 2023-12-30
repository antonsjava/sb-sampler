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

package sk.antons.sbsampler.rest;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sk.antons.jaul.util.Stk;
import sk.antons.sbutils.http.JsonExceptionHandler;

/**
 *
 * @author antons
 */
@ControllerAdvice(basePackages = "sk.antons.sbsampler.rest")
public class JsonExceptionAdvice {

     private static Logger log = LoggerFactory.getLogger(JsonExceptionAdvice.class);

     JsonExceptionHandler handler = JsonExceptionHandler.instance()
         .logger(t -> log.info("request failed {} ", Stk.trace(t)))
         // this is optional .statusResolver(JsonExceptionHandler.DefaultStatusResolver.instance().status(MyAppException.class, HttpStatus.CONFLICT))
         // this is default .processor(JsonExceptionHandler.DefaultExceptionProcessor.instance())
         //)
        ;

     @ExceptionHandler(Throwable.class)
     public ResponseEntity<ObjectNode> throwable(final Throwable ex) {
         return handler.process(ex);
     }

 }
