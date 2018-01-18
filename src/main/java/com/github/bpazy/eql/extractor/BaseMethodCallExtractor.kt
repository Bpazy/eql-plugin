package com.github.bpazy.eql.extractor

import com.intellij.openapi.project.Project

/**
 * @author ziyuan
 * created on 2018/1/15
 */
abstract class BaseMethodCallExtractor {
    abstract fun filter(methodName: String, argName: String?): Boolean

    abstract fun invoke(project: Project, arg: Any): Any
}
