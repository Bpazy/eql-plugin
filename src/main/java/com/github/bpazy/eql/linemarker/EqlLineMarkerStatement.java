package com.github.bpazy.eql.linemarker;

import com.github.bpazy.eql.Configs;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class EqlLineMarkerStatement implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) return null;
        if (!element.getText().equals("Dql")) return null;

        PsiJavaCodeReferenceElement referenceElement = PsiTreeUtil.getParentOfType(element, PsiJavaCodeReferenceElement.class);
        if (referenceElement == null) return null;
        PsiReference reference = referenceElement.getReference();
        if (reference == null) return null;
        if (!Configs.dqlName.equals(reference.getCanonicalText())) return null;

        PsiNewExpression psiNewExpression = PsiTreeUtil.getParentOfType(referenceElement, PsiNewExpression.class);
        if (psiNewExpression == null) return null;
        if (!"new Dql()".equals(psiNewExpression.getText())) return null;

        return new EqlLineMarkerInfo(element);
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }
}
