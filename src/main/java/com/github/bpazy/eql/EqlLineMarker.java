package com.github.bpazy.eql;

import com.google.common.collect.Lists;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class EqlLineMarker implements LineMarkerProvider {
    @Nullable
    @Override
    public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
        // todo add new Dql support
        if (!(element instanceof PsiIdentifier)) return null;
        if (!Configs.isEqlSql(element.getText())) return null;

        PsiClass psiClass = PsiTreeUtil.getParentOfType(element, PsiClass.class);
        PsiMethod psiMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

        if (psiClass == null || psiMethod == null) return null;
        if (!psiClass.hasAnnotation(Configs.eqlerConfigAntName)) return null;

        PsiAnnotation[] annotations = psiMethod.getAnnotations();
        if (ArrayUtils.isEmpty(annotations)) return null;

        List<String> antNames = Lists.newArrayList(annotations).stream().map(PsiAnnotation::getQualifiedName).collect(Collectors.toList());
        if (antNames.stream().noneMatch(Configs.eqlSqlAntNames::contains)) return null;

        PsiIdentifier nameIdentifier = psiMethod.getNameIdentifier();
        if (nameIdentifier == null) throw new RuntimeException();
        return new LineMarkerInfo<>(
                nameIdentifier,
                nameIdentifier.getTextRange(),
                Configs.eqlIcon,
                new Random().nextInt(10000),
                null,
                (e, elt) -> System.out.println(elt), // TODO 点击跳转eql文件
                GutterIconRenderer.Alignment.LEFT);
    }

    @Override
    public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {

    }
}
