package com.github.bpazy.eql.extractor;

import com.github.bpazy.eql.base.Configs;
import com.intellij.openapi.project.Project;
import com.intellij.psi.impl.source.tree.java.PsiExpressionListImpl;

/**
 * @author ziyuan
 * created on 2018/1/15
 */
public class UseSqlFileExtractor extends BaseMethodCallExtractor {
    @Override
    public boolean filter(String methodName, String argName) {
        return "useSqlFile".equals(methodName);
    }

    @Override
    public Object invoke(Project project, Object arg) {
        String classFileName = ((PsiExpressionListImpl) arg).getChildren()[1].getText();
        return classFileName.replace(".class", Configs.eqlFileExtension);
    }
}
