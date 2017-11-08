package com.github.bpazy.eql.intention;

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
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ziyuan
 * created on 2017/11/7
 */
public class JumpToJavaIntention extends BaseIntentionAction {
    private static final int NOT_EXIST_JAVA_METHOD = -1;

    private Pattern pattern = Pattern.compile("--\\s*\\[(.+)]");

    @NotNull
    @Override
    public String getText() {
        return "Jump to Java";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "eql";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        int startOffset = editor.getCaretModel().getVisualLineStart();
        int endOffset = editor.getCaretModel().getVisualLineEnd();
        String text = editor.getDocument().getText(new TextRange(startOffset, endOffset));
        if (StringUtils.isEmpty(text)) {
            return false;
        }

        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        String javaFileName = file.getName().replace(".eql", ".java");

        int startOffset = editor.getCaretModel().getVisualLineStart();
        int endOffset = editor.getCaretModel().getVisualLineEnd();
        String text = editor.getDocument().getText(new TextRange(startOffset, endOffset));

        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return;
        }
        String methodName = matcher.group(1);

        if (StringUtils.isEmpty(methodName)) {
            return;
        }

        String path = file.getVirtualFile().getPath();
        String[] split = path.split("resources/");
        if (split.length < 2) {
            return;
        }
        String eqlFilePackageName = split[1]
                .substring(0, split[1].lastIndexOf('/'))
                .replaceAll("/", ".");

        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(javaFileName);
        for (PsiFile file1 : files) {
            int lineNum = seekJavaMethod(eqlFilePackageName, file1, methodName);
            if (lineNum == NOT_EXIST_JAVA_METHOD) {
                continue;
            }

            // 打开对应java文件
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file1.getVirtualFile());
            Editor eqlEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (eqlEditor == null) return;
            // 跳转到java文件中函数的位置
            CaretModel caretModel = eqlEditor.getCaretModel();
            LogicalPosition logicalPosition = caretModel.getLogicalPosition();
            logicalPosition.leanForward(true);
            LogicalPosition logical = new LogicalPosition(lineNum, logicalPosition.column);
            caretModel.moveToLogicalPosition(logical);
        }
    }

    private int seekJavaMethod(String eqlFilePackageName, PsiFile file, String methodName) {
        Document document = FileDocumentManager.getInstance().getCachedDocument(file.getVirtualFile());
        if (document == null) {
            return NOT_EXIST_JAVA_METHOD;
        }
        String text = document.getText();
        BufferedReader reader = new BufferedReader(new StringReader(text));

        String packageName = ((PsiJavaFile) file).getPackageName();
        if (!eqlFilePackageName.equals(packageName)) {
            return NOT_EXIST_JAVA_METHOD;
        }

        try {
            int lineNum = 0;
            String s;
            while ((s = reader.readLine()) != null) {
                if (s.contains(methodName)) return lineNum;
                lineNum++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return NOT_EXIST_JAVA_METHOD;
    }
}
