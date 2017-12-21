package com.github.bpazy.eql.directory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ziyuan
 * created on 2017/12/21
 */
public enum EqlMethodDirectory {
    select,
    selectFirst,

    insert,

    delete,

    update;

    public static String toPatternString() {
        List<String> directories = Stream.of(values()).map(EqlMethodDirectory::name).collect(Collectors.toList());
        return String.join("|", directories);
    }
}
