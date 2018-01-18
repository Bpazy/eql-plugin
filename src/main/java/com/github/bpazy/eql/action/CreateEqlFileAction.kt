package com.github.bpazy.eql.action

import com.github.bpazy.eql.base.Configs
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiJavaFile
import java.io.File
import java.io.IOException

/**
 * @author ziyuan
 * created on 2017/11/17
 */
class CreateEqlFileAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) as? PsiJavaFile ?: return

        val path = psiFile.virtualFile.parent.path.replace("/java/", "/resources/")
        val fileByPath = LocalFileSystem.getInstance().findFileByPath(path)

        try {
            if (fileByPath == null) {
                val mkdirs = File(path).mkdirs()
                if (!mkdirs) throw IOException("创建文件夹失败:" + path)
            }

            val eqlPath = psiFile.virtualFile.path
                    .replace("/java/", "/resources/")
                    .replace(".java", Configs.eqlFileExtension)
            val eqlFile = File(eqlPath)
            val newFile = eqlFile.createNewFile()
            if (!newFile) throw IOException("创建文件失败:" + eqlPath)

            val project = e.getData(CommonDataKeys.PROJECT) ?: return

            val eqlVirtualFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(eqlFile) ?: return
            val descriptor = OpenFileDescriptor(project, eqlVirtualFile)
            FileEditorManager.getInstance(project).openTextEditor(descriptor, true)
        } catch (e1: IOException) {
            e1.printStackTrace()
        }

    }
}
