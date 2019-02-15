/*
 * Copyright 2019 Tango Controls
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

package org.tango.web.server.util;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Igor Khokhriakov <igor.khokhriakov@hzg.de>
 * @since 11/16/18
 */
public class WildcardExtractor {
    public List<Wildcard> extractWildcards(List<String> queryWildcards) {
        if(queryWildcards == null){
            return Collections.emptyList();
        }

        return queryWildcards.stream()
                .map(s -> {
                    String[] parts = s.split("/");

                    Wildcard result = new Wildcard();

                    result.host = parts.length >= 1 ? parts[0] : null;
                    result.domain = parts.length >= 2 ? parts[1] : null;
                    result.family = parts.length >= 3 ? parts[2] : null;
                    result.member = parts.length >= 4 ? parts[3] : null;
                    result.attribute = parts.length >= 5 ? parts[4] : null;

                    return result;
                }).collect(Collectors.toList());
    }

}
