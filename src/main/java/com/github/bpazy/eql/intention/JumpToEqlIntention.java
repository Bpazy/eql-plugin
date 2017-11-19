package com.github.bpazy.eql.intention;

import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ziyuan
 * created on 2017/10/28
 */
public class JumpToEqlIntention extends BaseIntentionAction {

    private final int NOT_EXIST_EQL_METHOD = -1;
    private final Pattern pattern = Pattern.compile("new (?:Dql|Eql).+(?:insert|delete|select|update)\\(\"(.+)\"\\)");

    @NotNull
    @Override
    public String getText() {
        return "Jump to eql";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "eql";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile psiFile) {
        int offset = editor.getCaretModel().getOffset();
        if (!(psiFile instanceof PsiJavaFile)) return false;

        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) return false;

        if (psiElement instanceof PsiWhiteSpace) {
            psiElement = psiElement.getPrevSibling();
        }
        PsiElement psiElement1 = extraEqlStatement(psiElement);
        if (psiElement1 == null) return false;

        String text = psiElement1.getText();
        return text.contains("new Eql") || text.contains("new Dql");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) return;

        String methodName = findEqlMethodName(psiElement);
        String eqlFileName = psiFile.getName().replace(".java", ".eql");
        String packageName = ((PsiJavaFile) psiFile).getPackageName();

        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(eqlFileName);
        for (PsiFile file : files) {
            int lineNum = seekEqlMethod(packageName, file, methodName);
            if (lineNum == NOT_EXIST_EQL_METHOD) continue;

            // 打开对应eql文件
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file.getVirtualFile());
            Editor eqlEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (eqlEditor == null) continue;

            // 将光标移动到eql文件中函数的位置
            CaretModel caretModel = eqlEditor.getCaretModel();
            LogicalPosition logicalPosition = caretModel.getLogicalPosition();
            logicalPosition.leanForward(true);
            LogicalPosition logical = new LogicalPosition(lineNum, logicalPosition.column);
            caretModel.moveToLogicalPosition(logical);

            // 将滚动条定位到光标位置
            ScrollingModel scrollingModel = editor.getScrollingModel();
            scrollingModel.scrollToCaret(ScrollType.RELATIVE);
            return;
        }
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

    private PsiElement extraEqlStatement(PsiElement element) {
        if (element == null ||
                element instanceof PsiExpressionStatement ||
                element instanceof PsiReturnStatement ||
                element instanceof PsiDeclarationStatement) {
            return element;
        }
        return extraEqlStatement(element.getParent());
    }

    /**
     * Eql文件中对应函数行号
     *
     * @param packageName java文件的package
     * @param file        eql文件
     * @param methodName  函数名称
     * @return 函数所在行号，-1则不存在
     */
    private int seekEqlMethod(String packageName, PsiFile file, String methodName) {
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
        if (!packageName.equals(eqlFilePackageName)) {
            return NOT_EXIST_EQL_METHOD;
        }

        try {
            int lineNum = 0;
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.matches("--\\s*\\[" + methodName + "]")) return lineNum;
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NOT_EXIST_EQL_METHOD;
    }
}
