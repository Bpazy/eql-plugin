package com.github.bpazy.eql.java2eql;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.impl.source.tree.java.PsiExpressionListImpl;
import com.intellij.psi.search.PsiShortNamesCache;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public class UseSqlFilePackageExtractor extends UseSqlFileExtractor {

    @Override
    public Object invoke(Project project, Object arg) {
        String javaFileName = ((PsiExpressionListImpl) arg).getChildren()[1].getText().replace(".class", ".java");
        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(javaFileName);
        return ((PsiJavaFile) files[0]).getPackageName();
    }
}
