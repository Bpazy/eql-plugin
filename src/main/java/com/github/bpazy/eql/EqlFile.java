package com.github.bpazy.eql;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.psi.PsiFile;
import lombok.Getter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class EqlFile {
    private PsiFile file;

    public EqlFile(PsiFile file) {
        this.file = file;
    }

    public String getEqlFilePackageName() {
        String path = file.getVirtualFile().getPath();
        String[] split = path.split("resources/");
        if (split.length < 2) return "";

        return split[1].substring(0, split[1].lastIndexOf('/')).replaceAll("/", ".");
    }

    public String getDocument() {
        Document document = FileDocumentManager.getInstance().getDocument(file.getVirtualFile());
        if (document == null) return "";

        return document.getText();
    }

    public LineNumberObjet getEqlMethodLineNumber(String eqlMethodName) {
        LineNumberObjet lineNumberObjet = new LineNumberObjet(eqlMethodName);
        lineNumberObjet.invoke();
        return lineNumberObjet;
    }

    public class LineNumberObjet {
        private String eqlMethodName;
        @Getter
        private int lineNum;
        @Getter
        private boolean found;

        public LineNumberObjet(String eqlMethodName) {
            this.eqlMethodName = eqlMethodName;
        }

        private void invoke() {
            BufferedReader reader = new BufferedReader(new StringReader(getDocument()));
            int lineNum = 0;
            try {
                while (true) {
                    String s = reader.readLine();
                    if (s == null) break;
                    if (s.trim().matches("--\\s*\\[" + eqlMethodName + "(\\s*[\\s\\S]*)?\\s*]")) {
                        this.lineNum = lineNum;
                        found = true;
                        break;
                    }
                    lineNum++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
