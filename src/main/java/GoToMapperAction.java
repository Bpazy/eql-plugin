import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.PsiShortNamesCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

/**
 * Created by Ziyuan
 * on 2017/10/12
 */
public class GoToMapperAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiElement psiElement = e.getData(PlatformDataKeys.PSI_ELEMENT);
        if (psiElement == null) return;


        PsiElement psiElementParent = psiElement.getParent();//获取方法的父元素
        if (psiElementParent == null) return;
        PsiFile containingFile = psiElementParent.getContainingFile();//获取到文件，这里是java类
        String className = containingFile.getName();//获取到类名
        String methodName = psiElement.toString().replace("PsiMethod:", "");
        String eqlFileName = className.replace(".java", ".eql");
        System.out.println("className: " + className);
        System.out.println("methodName: " + methodName);
        System.out.println("eqlFileName: " + eqlFileName);

        Project project = e.getProject();
        if (project == null) return;

        //查找名称为mapperName的文件
        PsiFile[] files = PsiShortNamesCache.getInstance(project).getFilesByName(eqlFileName);

        for (PsiFile file : files) {
            OpenFileDescriptor descriptor = new OpenFileDescriptor(project, file.getVirtualFile());
            Editor editor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true);
            if (editor == null) return;

            String text = editor.getDocument().getText();

            try {
                BufferedReader reader = new BufferedReader(new StringReader(text));

                int lineNum = 0;
                String s;
                while ((s = reader.readLine()) != null) {
                    if (s.contains("--[" + methodName + "]")) break;
                    lineNum++;
                }

                //定位到对应的sql
                CaretModel caretModel = editor.getCaretModel();
                LogicalPosition logicalPosition = caretModel.getLogicalPosition();
                logicalPosition.leanForward(true);
                LogicalPosition logical = new LogicalPosition(lineNum, logicalPosition.column);
                caretModel.moveToLogicalPosition(logical);
                SelectionModel selectionModel = editor.getSelectionModel();
                selectionModel.selectLineAtCaret();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
