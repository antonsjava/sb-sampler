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
package sk.antons.sbsampler.fail;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 *
 * @author antons
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValidationException extends RuntimeException {

    private Type type;
    private String field;
    private String value;

    private ValidationException(Type type) { super(type == Type.MANDATORY ? "Field is mandatory" : "field is in fromf format"); }
    public static ValidationException mandatory(String field) {
        ValidationException e = new ValidationException(Type.MANDATORY);
        e.type = Type.MANDATORY;
        e.field = field;
        return e;
    }
    public static ValidationException format(String field, String value) {
        ValidationException e = new ValidationException(Type.FORMAT);
        e.type = Type.FORMAT;
        e.field = field;
        e.value = value;
        return e;
    }

    public Type getType() { return type; }
    public String getField() { return field; }
    public String getValue() { return value; }

    public enum Type { MANDATORY, FORMAT }
}
