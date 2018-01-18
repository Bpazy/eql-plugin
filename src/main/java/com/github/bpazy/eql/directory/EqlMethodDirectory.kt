package com.github.bpazy.eql.directory

/**
 * @author ziyuan
 * created on 2017/12/21
 */
enum class EqlMethodDirectory {
    select,
    selectFirst,

    insert,

    delete,

    update;


    companion object {

        fun toPatternString(): String {
            val directories = values().map { v -> v.name }
            return directories.joinToString("|")
        }
    }
}
