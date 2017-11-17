package com.github.bpazy.eql;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import java.io.IOException;

/**
 * @author ziyuan
 * created on 2017/11/17
 */
public class CreateEqlFileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
        if (!(psiFile instanceof PsiJavaFile)) return;

        LocalFileSystem localFileSystem = LocalFileSystem.getInstance();
        String path = psiFile.getVirtualFile().getParent().getPath().replace("/java/", "/resources/");
        VirtualFile fileByPath = localFileSystem.findFileByPath(path);
        if (fileByPath == null) return;

        try {
            localFileSystem.createChildFile(null, fileByPath, psiFile.getName().replace(".java", ".eql"));
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
