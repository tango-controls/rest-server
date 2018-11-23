package org.tango.web.server.util;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
