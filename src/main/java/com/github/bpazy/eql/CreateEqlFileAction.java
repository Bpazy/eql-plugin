package com.github.bpazy.eql;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;

import java.io.File;
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

        String path = psiFile.getVirtualFile().getParent().getPath().replace("/java/", "/resources/");
        VirtualFile fileByPath = LocalFileSystem.getInstance().findFileByPath(path);

        try {
            if (fileByPath == null) {
                boolean mkdirs = new File(path).mkdirs();
                if (!mkdirs) throw new IOException("创建文件夹失败:" + path);
            }
            String eqlPath = psiFile.getVirtualFile().getPath()
                    .replace("/java/", "/resources/")
                    .replace(".java", ".eql");
            boolean newFile = new File(eqlPath).createNewFile();
            if (!newFile) throw new IOException("创建文件失败:" + eqlPath);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
