package com.github.bpazy.eql.statement;

import com.github.bpazy.eql.directory.EqlMethodDirectory;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ziyuan
 * created on 2018/1/8
 */
public class EqlStatement {
    private static final Pattern pattern = Pattern.compile("new (?:Dql|Eql).+(?:" + EqlMethodDirectory.toPatternString() + ")\\(\"(.+)\"\\)");
    public static final int NOT_EXIST_EQL_METHOD = -1;

    private PsiElement psiElement;
    private PsiFile psiFile;

    public EqlStatement(PsiElement psiElement) {
        this.psiElement = psiElement;
        this.psiFile = psiElement.getContainingFile();
    }

    public String eqlMethodName() {
        return findEqlMethodName(psiElement);
    }

    public String eqlFileName() {
        return psiFile.getName().replace(".java", ".eql");
    }

    public String packageName() {
        return ((PsiJavaFile) psiFile).getPackageName();
    }

    /**
     * Eql文件中对应函数行号
     *
     * @param file eql文件
     * @return 函数所在行号，-1则不存在
     */
    public int seekEqlMethod(PsiFile file) {
        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) {
            return NOT_EXIST_EQL_METHOD;
        }
        String text = document.getText();
        BufferedReader reader = new BufferedReader(new StringReader(text));

        String path = file.getVirtualFile().getPath();
        String[] split = path.split("resources/");
        if (split.length < 2) {
            return NOT_EXIST_EQL_METHOD;
        }
        String eqlFilePackageName = split[1]
                .substring(0, split[1].lastIndexOf('/'))
                .replaceAll("/", ".");
        if (!packageName().equals(eqlFilePackageName)) {
            return NOT_EXIST_EQL_METHOD;
        }

        try {
            int lineNum = 0;
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.matches("--\\s*\\[" + eqlMethodName() + "]")) return lineNum;
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NOT_EXIST_EQL_METHOD;
    }

    private String findEqlMethodName(PsiElement psiElement) {
        if (psiElement instanceof PsiMethodCallExpression) {
            String methodCallExpression = psiElement.getText();
            Matcher matcher = pattern.matcher(methodCallExpression);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        if (psiElement instanceof PsiMethod) {
            return ((PsiMethod) psiElement).getName();
        }
        return findEqlMethodName(psiElement.getParent());
    }
}
