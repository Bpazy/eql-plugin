import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

/**
 * @author ziyuan
 * created on 2017/10/28
 */
public class QuickFix extends BaseIntentionAction {

    @NotNull
    @Override
    public String getText() {
        return "Fiiiiiix";
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "my test quick fix";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        return true;
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, PsiFile file) throws IncorrectOperationException {
        System.out.println(project);
        System.out.println(editor);
        System.out.println(file);
    }
}
