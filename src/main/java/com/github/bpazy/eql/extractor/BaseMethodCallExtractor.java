package com.github.bpazy.eql.extractor;

import com.intellij.openapi.project.Project;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public abstract class BaseMethodCallExtractor {
    public abstract boolean filter(String methodName, String argName);

    public abstract Object invoke(Project project, Object arg);
}
