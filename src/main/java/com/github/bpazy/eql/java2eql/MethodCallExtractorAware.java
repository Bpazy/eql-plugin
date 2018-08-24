package com.github.bpazy.eql.java2eql;

import com.intellij.openapi.project.Project;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public interface MethodCallExtractorAware {
    boolean filter(String methodName, String argName);

    Object invoke(Project project, Object arg);
}
