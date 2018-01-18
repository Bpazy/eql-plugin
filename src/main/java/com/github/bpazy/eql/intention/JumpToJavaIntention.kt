package com.github.bpazy.eql.intention

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.intention.impl.BaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.LogicalPosition
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.java.PsiLiteralExpressionImpl
import com.intellij.psi.search.PsiShortNamesCache
import com.intellij.util.IncorrectOperationException
import org.apache.commons.lang.StringUtils
import org.jetbrains.annotations.Nls
import java.util.*
import java.util.regex.Pattern

/**
 * @author ziyuan
 * created on 2017/11/7
 */
class JumpToJavaIntention : BaseIntentionAction() {

    private val pattern = Pattern.compile("--\\s*\\[(.+)]")

    override fun getText(): String {
        return "Jump to Java"
    }

    @Nls
    override fun getFamilyName(): String {
        return "eql"
    }

    override fun isAvailable(project: Project, editor: Editor, file: PsiFile): Boolean {
        val startOffset = editor.caretModel.visualLineStart
        val endOffset = editor.caretModel.visualLineEnd

        val text = editor.document.getText(TextRange(startOffset, endOffset))
        if (StringUtils.isEmpty(text)) return false

        val matcher = pattern.matcher(text)
        return matcher.find()
    }

    @Throws(IncorrectOperationException::class)
    override fun invoke(project: Project, editor: Editor, file: PsiFile) {
        val fileName = file.name
        val javaFileName = fileName.substring(0, fileName.lastIndexOf(".")) + ".java"

        val startOffset = editor.caretModel.visualLineStart
        val endOffset = editor.caretModel.visualLineEnd
        val text = editor.document.getText(TextRange(startOffset, endOffset))

        val matcher = pattern.matcher(text)
        if (!matcher.find()) return

        val methodName = matcher.group(1)
        if (StringUtils.isEmpty(methodName)) return

        val path = file.virtualFile.path
        val split = path.split("resources/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (split.size < 2) {
            return
        }
        val eqlFilePackageName = split[1]
                .substring(0, split[1].lastIndexOf('/'))
                .replace("/".toRegex(), ".")

        val files = PsiShortNamesCache.getInstance(project).getFilesByName(javaFileName)
        for (file1 in files) {
            val methodElement = findElement(eqlFilePackageName, file1, methodName) ?: continue

            val document = PsiDocumentManager.getInstance(project).getDocument(file1) ?: continue

            val textOffset = methodElement.textOffset
            val lineNumber = document.getLineNumber(textOffset)

            // 打开对应java文件
            val descriptor = OpenFileDescriptor(project, file1.virtualFile)
            val javaEditor = FileEditorManager.getInstance(project).openTextEditor(descriptor, true) ?: continue

            // 将光标移动到java文件中函数的位置
            val caretModel = javaEditor.caretModel
            val logicalPosition = caretModel.logicalPosition
            logicalPosition.leanForward(true)
            val logical = LogicalPosition(lineNumber, document.getLineStartOffset(lineNumber))
            caretModel.moveToLogicalPosition(logical)

            // 将滚动条定位到光标位置
            val scrollingModel = javaEditor.scrollingModel
            scrollingModel.scrollToCaret(ScrollType.MAKE_VISIBLE)
            return
        }
        HintManager.getInstance().showErrorHint(editor, "Java method not found")
    }

    /**
     * 查找Java element
     *
     * @param eqlFilePackageName eql文件对应的packageName
     * @param file               Java对应文件的PsiFile
     * @param methodName         方法名称
     * @return 查找到的Java方法，没有则返回null
     */
    private fun findElement(eqlFilePackageName: String, file: PsiFile, methodName: String): PsiElement? {
        val packageName = (file as PsiJavaFile).packageName
        if (eqlFilePackageName != packageName) return null

        // 寻找Java方法
        val stack = Stack<PsiElement>()
        stack.push(file)
        while (!stack.empty()) {
            val element = stack.pop()
            if (element is PsiIdentifier && element.getParent() is PsiMethod) {
                val methodText = element.getText()
                if (methodName == methodText) return element
            }
            element.children.forEach { stack.push(it) }
        }

        // 寻找字符串常量
        stack.push(file)
        while (!stack.empty()) {
            val element = stack.pop()
            val parent = element.parent
            if (element is PsiJavaToken && parent is PsiLiteralExpression) {
                val methodText = (parent as PsiLiteralExpressionImpl).innerText
                if (methodName == methodText) return parent
            }
            element.children.forEach { stack.push(it) }
        }
        return null
    }
}
