package com.github.bpazy.eql.intention;

import com.github.bpazy.eql.statement.EqlStatement;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author ziyuan
 * created on 2017/10/28
 */
public class JumpToEqlIntention extends BaseIntentionAction {

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

        EqlStatement eqlStatement = new EqlStatement(psiElement);

        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(eqlStatement.eqlFileName());
        for (PsiFile file : files) {
            int lineNum = eqlStatement.seekEqlMethod(file);
            if (lineNum == EqlStatement.NOT_EXIST_EQL_METHOD) continue;

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
            ScrollingModel scrollingModel = eqlEditor.getScrollingModel();
            scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE);
            return;
        }
        HintManager.getInstance().showErrorHint(editor, "Eql method not found");
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
}
