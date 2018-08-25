package com.github.bpazy.eql.java2eql;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.util.PsiTreeUtil;

public class EqlInterface implements EqlAware {
    private PsiClass psiClass;
    private PsiElement psiElement;

    public EqlInterface(PsiClass psiClass, PsiElement psiElement) {
        this.psiClass = psiClass;
        this.psiElement = psiElement;
    }

    @Override
    public String getEqlFileName() {
        return psiClass.getName() + ".eql";
    }

    @Override
    public String getEqlMethodName() {
        PsiMethod eqlMethod = PsiTreeUtil.getParentOfType(psiElement, PsiMethod.class);
        if (eqlMethod == null) return "";

        // TODO support UseSqlFile
//        PsiAnnotation useSqlFileAnnotation = eqlMethod.getAnnotation("org.n3r.eql.eqler.annotations.UseSqlFile");
//        if (useSqlFileAnnotation == null) return eqlMethod.getName();
//
//        List<JvmAnnotationAttribute> attributes = useSqlFileAnnotation.getAttributes();
//        Optional<JvmAnnotationAttribute> clazzOptional = attributes.stream()
//                .filter(attr -> attr.getAttributeName().equals("clazz"))
//                .findFirst();


        return eqlMethod.getName();
    }

    @Override
    public String getPackageName() {
        return ((PsiJavaFileImpl) psiElement.getContainingFile()).getPackageName();
    }
}
