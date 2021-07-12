package com.haivo.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ImportPictureAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 此处的代码会在Action被点击后执行
        new ImportPictureDialog(e).show();
    }
}
