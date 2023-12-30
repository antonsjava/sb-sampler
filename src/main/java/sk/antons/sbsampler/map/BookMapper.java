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
package sk.antons.sbsampler.map;


import org.mapstruct.CollectionMappingStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.ReportingPolicy;
import sk.antons.sbsampler.model.Author;
import sk.antons.sbsampler.model.Book;
import sk.antons.sbsampler.ws.book.WsAuthor;
import sk.antons.sbsampler.ws.book.WsBook;


/**
 *
 * @author antons
 */
//@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, componentModel = "spring", collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY)
@Mapper(unmappedTargetPolicy = ReportingPolicy.ERROR, collectionMappingStrategy = CollectionMappingStrategy.ACCESSOR_ONLY)
public abstract class BookMapper {

    public abstract Author toAuthor(WsAuthor value);
    @Mappings({
        @Mapping( target = "abstractText", source = "abstract")
        , @Mapping( target = "author", ignore = true)
    })
    public abstract Book toBook(WsBook value);


}
