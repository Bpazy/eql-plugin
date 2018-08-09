package com.github.bpazy.eql.statement;

import com.github.bpazy.eql.base.Configs;
import com.github.bpazy.eql.directory.EqlMethodDirectory;
import com.github.bpazy.eql.extractor.BaseMethodCallExtractor;
import com.github.bpazy.eql.extractor.UseSqlFileExtractor;
import com.github.bpazy.eql.extractor.UseSqlFilePackageExtractor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ziyuan
 * created on 2018/1/8
 */
public class EqlStatement {
    public static final int NOT_EXIST_EQL_METHOD = -1;
    private static final Pattern pattern = Pattern.compile("new (?:Dql|Eql)[\\s\\S]+(?:" + EqlMethodDirectory.toPatternString() + ")\\(\"(.+)\"\\)");
    private static final Pattern statementPattern = Pattern.compile("new (?:Dql|Eql).+execute\\(\\)");

    private Project project;
    private PsiElement psiElement;
    private PsiFile psiFile;
    private String useSqlFileEqlFileName;
    private String useSqlFileEqlPackageName;

    /**
     * 包含完整eql执行流程的表达式
     */
    private PsiMethodCallExpression fullEqlExpression;

    public EqlStatement(Project project, PsiElement psiElement) {
        this.project = project;

        this.psiElement = psiElement;
        this.psiFile = psiElement.getContainingFile();

        this.fullEqlExpression = initEqlStatement(this.psiElement);


        initExtractor();
    }

    private void initExtractor() {
        BaseMethodCallExtractor useSqlFileExtractor = new UseSqlFileExtractor();
        BaseMethodCallExtractor useSqlFilePackageNameExtractor = new UseSqlFilePackageExtractor();


        Stack<PsiMethodCallExpression> stack = new Stack<>();
        stack.push(fullEqlExpression);
        while (!stack.isEmpty()) {
            PsiMethodCallExpression psiMethodCallExpression = stack.pop();
            PsiReferenceExpression methodExpression = psiMethodCallExpression.getMethodExpression();


            // Add features here
            String identifierName = methodExpression.getLastChild().getText();
            if (useSqlFileExtractor.filter(identifierName, null)) {
                useSqlFileEqlFileName = (String) useSqlFileExtractor.invoke(project, psiMethodCallExpression.getArgumentList());
            }
            if (useSqlFilePackageNameExtractor.filter(identifierName, null)) {
                useSqlFileEqlPackageName = (String) useSqlFilePackageNameExtractor.invoke(project, psiMethodCallExpression.getArgumentList());
            }


            PsiExpression qualifierExpression = methodExpression.getQualifierExpression();
            if (qualifierExpression instanceof PsiMethodCallExpression) {
                stack.push((PsiMethodCallExpression) qualifierExpression);
            }
        }
    }

    private PsiMethodCallExpression initEqlStatement(PsiElement element) {
        if (element instanceof PsiMethodCallExpression) {
            String methodCallExpression = element.getText().replaceAll("[\r\n]", "");
            Matcher matcher = statementPattern.matcher(methodCallExpression);
            if (matcher.find()) {
                return (PsiMethodCallExpression) element;
            }
        }
        return initEqlStatement(element.getParent());
    }

    private String getEqlMethodName() {
        return findEqlMethodName(psiElement);
    }

    public String getEqlFileName() {
        if (StringUtils.isNotEmpty(useSqlFileEqlFileName)) return useSqlFileEqlFileName;

        return psiFile.getName().replace(".java", Configs.eqlFileExtension);
    }

    private String getPackageName() {
        if (StringUtils.isNotEmpty(useSqlFileEqlPackageName)) return useSqlFileEqlPackageName;

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
        if (!getPackageName().equals(eqlFilePackageName)) {
            return NOT_EXIST_EQL_METHOD;
        }

        try {
            int lineNum = 0;
            String s;
            while ((s = reader.readLine()) != null) {
                s = s.trim();
                if (s.matches("--\\s*\\[" + getEqlMethodName() + "(\\s*[\\s\\S]*)?\\s*]")) return lineNum;
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
