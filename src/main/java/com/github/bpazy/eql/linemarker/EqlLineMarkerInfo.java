package com.github.bpazy.eql.linemarker;

import com.github.bpazy.eql.Configs;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiElement;

import java.util.Random;

public class EqlLineMarkerInfo extends LineMarkerInfo<PsiElement> {
    public EqlLineMarkerInfo(PsiElement element) {
        super(element, element.getTextRange(), Configs.eqlIcon, new Random().nextInt(10000), null,
                (e, elt) -> System.out.println(elt), // TODO 点击跳转eql文件
                GutterIconRenderer.Alignment.LEFT);
    }
}
