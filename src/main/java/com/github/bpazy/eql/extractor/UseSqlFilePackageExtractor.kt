package com.github.bpazy.eql.extractor

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.impl.source.tree.java.PsiExpressionListImpl
import com.intellij.psi.search.PsiShortNamesCache

/**
 * @author ziyuan
 * created on 2018/1/15
 */
class UseSqlFilePackageExtractor : UseSqlFileExtractor() {

    override fun invoke(project: Project, arg: Any): Any {
        val javaFileName = (arg as PsiExpressionListImpl).children[1].text.replace(".class", ".java")
        val files = PsiShortNamesCache.getInstance(project).getFilesByName(javaFileName)
        return (files[0] as PsiJavaFile).packageName
    }
}
