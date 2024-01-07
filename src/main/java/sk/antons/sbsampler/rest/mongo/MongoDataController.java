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
package sk.antons.sbsampler.rest.mongo;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import sk.antons.jaul.Is;
import sk.antons.jaul.Split;
import sk.antons.jaul.util.AsRuntimeEx;
import sk.antons.jaul.util.Resource;
import sk.antons.jmom.Jmom;
import sk.antons.jmom.rule.Rule;
import sk.antons.json.JsonFactory;
import sk.antons.json.JsonValue;
import sk.antons.json.parse.JsonParser;


/**
 * Example of reading mongo export
 * @author antons
 */
@RestController
@RequestMapping(path="/mongo")
public class MongoDataController {
    private static Logger log = LoggerFactory.getLogger(MongoDataController.class);

    @Autowired ObjectMapper om;

    @GetMapping(path="/transfers"
        , produces = MediaType.APPLICATION_JSON_VALUE
    )
    public @ResponseBody List<Transfer> readdbexport() {

        try {

            Jmom mongo = mongoJmom();

            List<Transfer> transfers = new ArrayList<>();
            // read mongo export line by line
            Iterator<String> iter = Split.file(Resource.url("classpath:db/transfers.log").inputStream(), "utf-8").byLines();
            while(iter.hasNext()) {
                String line = iter.next();
                //parse monlo export line to json
                JsonValue jv = JsonParser.parse(line);
                log.debug("input line:     {}", line);
                //convert json from mongo stupid format
                mongo.apply(jv);
                log.debug("processed line: {}", jv.toCompactString());
                Transfer transfer = om.readValue(jv.toCompactString(), Transfer.class);
                transfers.add(transfer);
            }

            return transfers;

        } catch(Exception e) {
            throw AsRuntimeEx.argument(e);
        }
    }


    public static Jmom mongoJmom() {
        Jmom jmom = Jmom.instance()
                .apply(new Rule() { //convert _class to class and just simple name
                        @Override
                        public void apply(JsonValue json) {
                            if(json.isStringLiteral()) {
                                String value = json.asStringLiteral().stringValue();
                                int pos = value.lastIndexOf(".");
                                if(pos > -1) {
                                    value = value.substring(pos+1);
                                }
                                JsonValue parent = json.parent();
                                if(parent.isObject()) {
                                    parent.asObject().add("class", JsonFactory.stringLiteral(value));
                                    parent.asObject().removeAll("_class");
                                }
                            }
                        }
                    }, "**/_class")
                .apply(new Rule() { // convert date object to string literal
                        @Override
                        public void apply(JsonValue json) {
                            if(json.isIntLiteral()) {
                                long value = json.asIntLiteral().longValue();
                                Date date = new Date(value);
                                SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                String svalue = sf.format(date);
                                JsonValue parent = json.parent();
                                if(parent.isObject()) {
                                    parent.replaceBy(JsonFactory.stringLiteral(svalue));
                                }
                            } else if(json.isStringLiteral()) {
                                String svalue = json.asStringLiteral().stringValue();
                                JsonValue parent = json.parent();
                                if(parent.isObject()) {
                                    parent.replaceBy(JsonFactory.stringLiteral(svalue));
                                }
                            } else if(json.isObject()) {
                                JsonValue jv = json.asObject().first("$numberLong");
                                if(jv != null) {
                                    String svalue = jv.asStringLiteral().stringValue();
                                    if(!Is.empty(svalue)) {
                                        Long l = null;
                                        try {
                                            l = Long.parseLong(svalue);
                                        } catch(Exception e) {
                                        }
                                        if(l != null) {
                                            Date date = new Date(l);
                                            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                            svalue = sf.format(date);
                                            JsonValue parent = json.parent();
                                            if(parent.isObject()) {
                                                parent.replaceBy(JsonFactory.stringLiteral(svalue));
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }, "**/$date")
                ;
        return jmom;
    }
}