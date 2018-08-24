package com.github.bpazy.eql.java2eql;

import com.github.bpazy.eql.EqlFile;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author ziyuan
 * created on 2017/10/28
 */
public class StatementToEqlFileIntention extends BaseIntentionAction {

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

        PsiClass eqlInterfacePsiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        if (eqlInterfacePsiClass == null) return false;
        if (eqlInterfacePsiClass.getAnnotation("org.n3r.eql.eqler.annotations.EqlerConfig") != null) return true;

        if (psiElement instanceof PsiWhiteSpace) {
            psiElement = psiElement.getPrevSibling();
        }
        PsiElement psiElement1 = extraEqlStatement(psiElement);
        if (psiElement1 == null) return false;

        String text = psiElement1.getText();
        return text.contains("new Eql") || text.contains("new Dql");
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

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) return;

        PsiClass psiClass = PsiTreeUtil.getParentOfType(psiElement, PsiClass.class);
        EqlAware eqlInterface;
        if (psiClass != null && psiClass.hasAnnotation("org.n3r.eql.eqler.annotations.EqlerConfig")) {
            eqlInterface = new EqlInterface(psiClass);
        } else {
            eqlInterface = new EqlStatement(project, psiElement);
        }


        String eqlFileName = eqlInterface.getEqlFileName();
        PsiFile[] sameNameEqlFiles = PsiShortNamesCache.getInstance(project).getFilesByName(eqlFileName);
        for (PsiFile file : sameNameEqlFiles) {
            EqlFile eqlFile = new EqlFile(file);
            if (!eqlInterface.getPackageName().equals(eqlFile.getEqlFilePackageName())) continue;

            EqlFile.LineNumberObjet lineNumberObject = eqlFile.getEqlMethodLineNumber(eqlInterface.getEqlMethodName());
            if (!lineNumberObject.isFound()) continue;

            boolean success = jump2eql(project, file, lineNumberObject.getLineNum());
            if (success) return;
        }
        HintManager.getInstance().showErrorHint(editor, "Eql method not found");
    }

    private boolean jump2eql(@NotNull Project project, PsiFile file, int lineNum) {
        // 打开对应eql文件
        OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file.getVirtualFile());
        Editor eqlEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
        if (eqlEditor == null) return false;

        // 将光标移动到eql文件中函数的位置
        CaretModel caretModel = eqlEditor.getCaretModel();
        LogicalPosition logicalPosition = caretModel.getLogicalPosition();
        logicalPosition.leanForward(true);
        LogicalPosition logical = new LogicalPosition(lineNum, logicalPosition.column);
        caretModel.moveToLogicalPosition(logical);

        // 将滚动条定位到光标位置
        ScrollingModel scrollingModel = eqlEditor.getScrollingModel();
        scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
        return true;
    }
}
