package com.github.bpazy.eql.extractor

import com.github.bpazy.eql.base.Configs
import com.intellij.openapi.project.Project
import com.intellij.psi.impl.source.tree.java.PsiExpressionListImpl

/**
 * @author ziyuan
 * created on 2018/1/15
 */
open class UseSqlFileExtractor : BaseMethodCallExtractor() {
    override fun filter(methodName: String, argName: String?): Boolean {
        return "useSqlFile" == methodName
    }

    override fun invoke(project: Project, arg: Any): Any {
        val classFileName = (arg as PsiExpressionListImpl).children[1].text
        return classFileName.replace(".class", Configs.eqlFileExtension)
    }
}
