package com.github.bpazy.eql.statement

import com.github.bpazy.eql.base.Configs
import com.github.bpazy.eql.directory.EqlMethodDirectory
import com.github.bpazy.eql.extractor.UseSqlFileExtractor
import com.github.bpazy.eql.extractor.UseSqlFilePackageExtractor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import org.apache.commons.lang.StringUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern

/**
 * @author ziyuan
 * created on 2018/1/8
 */
class EqlStatement(private val project: Project, private val psiElement: PsiElement) {
    private val psiFile: PsiFile = psiElement.containingFile
    private var useSqlFileEqlFileName: String? = null
    private var useSqlFileEqlPackageName: String? = null

    /**
     * 包含完整eql执行流程的表达式
     */
    private val fullEqlExpression: PsiMethodCallExpression

    init {
        this.fullEqlExpression = initEqlStatement(this.psiElement)
        initExtractor()
    }

    private fun initExtractor() {
        val useSqlFileExtractor = UseSqlFileExtractor()
        val useSqlFilePackageNameExtractor = UseSqlFilePackageExtractor()


        val stack = Stack<PsiMethodCallExpression>()
        stack.push(fullEqlExpression)
        while (!stack.isEmpty()) {
            val psiMethodCallExpression = stack.pop()
            val methodExpression = psiMethodCallExpression.methodExpression


            // Add features here
            val identifierName = methodExpression.lastChild.text
            if (useSqlFileExtractor.filter(identifierName, null)) {
                useSqlFileEqlFileName = useSqlFileExtractor.invoke(project, psiMethodCallExpression.argumentList) as String
            }
            if (useSqlFilePackageNameExtractor.filter(identifierName, null)) {
                useSqlFileEqlPackageName = useSqlFilePackageNameExtractor.invoke(project, psiMethodCallExpression.argumentList) as String
            }


            val qualifierExpression = methodExpression.qualifierExpression
            if (qualifierExpression is PsiMethodCallExpression) {
                stack.push(qualifierExpression as PsiMethodCallExpression?)
            }
        }
    }

    private fun initEqlStatement(element: PsiElement): PsiMethodCallExpression {
        if (element is PsiMethodCallExpression) {
            val methodCallExpression = element.getText().replace("[\r\n]".toRegex(), "")
            val matcher = statementPattern.matcher(methodCallExpression)
            if (matcher.find()) {
                return element
            }
        }
        return initEqlStatement(element.parent)
    }

    private fun eqlMethodName() = findEqlMethodName(psiElement)

    public fun eqlFileName(): String? {
        return if (useSqlFileEqlFileName.isNullOrEmpty()) useSqlFileEqlFileName
        else psiFile.name.replace(".java", Configs.eqlFileExtension)
    }

    private fun packageName(): String? {
        return if (StringUtils.isNotEmpty(useSqlFileEqlPackageName)) useSqlFileEqlPackageName
        else (psiFile as? PsiJavaFile)?.packageName
    }

    /**
     * Eql文件中对应函数行号
     *
     * @param file eql文件
     * @return 函数所在行号，null则不存在
     */
    fun seekEqlMethod(file: PsiFile): Int? {
        val document = FileDocumentManager.getInstance().getDocument(file.virtualFile) ?: return null
        val reader = BufferedReader(StringReader(document.text))

        val split = file.virtualFile.path
                .split("resources/".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (split.size < 2) return null

        val eqlFilePackageName = split[1]
                .substring(0, split[1].lastIndexOf('/'))
                .replace("/".toRegex(), ".")
        if (eqlFilePackageName != packageName()) return null

        try {
            var lineNum = 0
            var s: String? = reader.readLine()
            while (s != null) {
                if (s.matches("--\\s*\\[${eqlMethodName()}]".toRegex())) return lineNum
                lineNum++
                s = reader.readLine()
                if (s == null) {
                    break
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun findEqlMethodName(psiElement: PsiElement): String {
        if (psiElement is PsiMethodCallExpression) {
            val methodCallExpression = psiElement.getText()
            val matcher = pattern.matcher(methodCallExpression)
            if (matcher.find()) {
                return matcher.group(1)
            }
        }
        return (psiElement as? PsiMethod)?.name ?: findEqlMethodName(psiElement.parent)
    }

    companion object {
        private val pattern = Pattern.compile("new (?:Dql|Eql)[\\s\\S]+(?:${EqlMethodDirectory.toPatternString()})\\(\"(.+)\"\\)")
        private val statementPattern = Pattern.compile("new (?:Dql|Eql).+execute\\(\\)")
    }
}
