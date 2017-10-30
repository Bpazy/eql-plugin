import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.tree.java.PsiJavaTokenImpl;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

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
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) {
            return false;
        }
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(((PsiJavaTokenImpl) psiElement).getStartOffset());

        String text = psiElement.getText();
        return text.contains("new Eql") || text.contains("new Dql");
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile psiFile) throws IncorrectOperationException {
        int offset = editor.getCaretModel().getOffset();
        PsiElement psiElement = psiFile.findElementAt(offset);
        if (psiElement == null) {
            return;
        }
        String methodName = psiElement.getText();
        String eqlFileName = psiFile.getName().replace(".java", ".eql");

        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(eqlFileName);
        for (PsiFile file : files) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file.getVirtualFile());
            Editor eqlEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (eqlEditor == null) return;

            BufferedReader reader = new BufferedReader(new StringReader(eqlFileName));
            try {
                int lineNum = 0;
                String s;
                while ((s = reader.readLine()) != null) {
                    if (s.contains("--[" + methodName + "]")) break;
                    lineNum++;
                }
                CaretModel caretModel = editor.getCaretModel();
                LogicalPosition logicalPosition = caretModel.getLogicalPosition();
                logicalPosition.leanForward(true);
                LogicalPosition logical = new LogicalPosition(lineNum, logicalPosition.column);
                caretModel.moveToLogicalPosition(logical);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
