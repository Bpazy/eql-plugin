package com.github.bpazy.eql.java2eql;

import com.intellij.psi.PsiClass;

/**
 * TODO
 */
public class EqlInterface implements EqlAware {
    private PsiClass eqlInterfacePsiClass;

    public EqlInterface(PsiClass eqlInterfacePsiClass) {
        this.eqlInterfacePsiClass = eqlInterfacePsiClass;
    }

    @Override
    public String getEqlFileName() {
        return null;
    }

    @Override
    public String getEqlMethodName() {
        return null;
    }

    @Override
    public String getPackageName() {
        return null;
    }
}
