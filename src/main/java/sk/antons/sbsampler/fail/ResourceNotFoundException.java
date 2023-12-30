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

/**
 *
 * @author antons
 */
public class ResourceNotFoundException extends RuntimeException {

    private String resource;
    private String id;

    private ResourceNotFoundException(String resource, String id) {
        super("resource "+resource+"."+id+" not found ");
        this.resource = resource;
        this.id = id;
    }
    public static ResourceNotFoundException instance(String resource, String id) { return new ResourceNotFoundException(resource, id); }

    public String getResource() { return resource; }
    public String getId() { return id; }

    public static int httpCode() { return 404; }
}
