package com.github.bpazy.eql.intention

import com.github.bpazy.eql.statement.EqlStatement
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.IncorrectOperationException
import org.jetbrains.annotations.Nls

/**
 * @author ziyuan
 * created on 2017/10/28
 */
class JumpToEqlIntention : BaseIntentionAction() {

    override fun getText(): String {
        return "Jump to eql"
    }

    @Nls
    override fun getFamilyName(): String {
        return "eql"
    }

    override fun isAvailable(project: Project, editor: Editor, psiFile: PsiFile): Boolean {
        val offset = editor.caretModel.offset
        if (psiFile !is PsiJavaFile) return false

        var psiElement: PsiElement? = psiFile.findElementAt(offset) ?: return false

        if (psiElement is PsiWhiteSpace) {
            psiElement = psiElement.prevSibling
        }
        val psiElement1 = extraEqlStatement(psiElement) ?: return false

        val text = psiElement1.text
        return text.contains("new Eql") || text.contains("new Dql")
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, psiFile: PsiFile) {
        val offset = editor.caretModel.offset
        val psiElement = psiFile.findElementAt(offset) ?: return

        val eqlStatement = EqlStatement(project, psiElement)

        val files = PsiShortNamesCache.getInstance(project).getFilesByName(eqlStatement.eqlFileName!!)
        for (file in files) {
            val lineNum = eqlStatement.seekEqlMethod(file)
            if (lineNum == EqlStatement.NOT_EXIST_EQL_METHOD) continue

            // 打开对应eql文件
            val descriptor = OpenFileDescriptor(project, file.virtualFile)
            val eqlEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true) ?: continue

            // 将光标移动到eql文件中函数的位置
            val caretModel = eqlEditor.caretModel
            val logicalPosition = caretModel.logicalPosition
            logicalPosition.leanForward(true)
            val logical = LogicalPosition(lineNum, logicalPosition.column)
            caretModel.moveToLogicalPosition(logical)

            // 将滚动条定位到光标位置
            val scrollingModel = eqlEditor.scrollingModel
            scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            return
        }
        HintManager.getInstance().showErrorHint(editor, "Eql method not found")
    }

    private fun extraEqlStatement(element: PsiElement?): PsiElement? {
        return if (element == null ||
                element is PsiExpressionStatement ||
                element is PsiReturnStatement ||
                element is PsiDeclarationStatement) {
            element
        } else extraEqlStatement(element.parent)
    }
}
