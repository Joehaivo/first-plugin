package com.haivo.plugin;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportPictureDialog extends DialogWrapper {
    private JPanel contentPane;
    private TextFieldWithBrowseButton externalChooseBtn;
    private TextFieldWithBrowseButton targetChooseBtn;
    private JList<String> mapperList;
    private JScrollPane scrollPane;

    private Project project;
    private List<VirtualFile> externalFiles = new ArrayList<>();
    private VirtualFile targetFolder;

    public ImportPictureDialog(AnActionEvent event) {
        super(event.getProject());
        this.project = event.getProject();
        init();
        initUI(event);
    }

    private void initUI(AnActionEvent event) {
        contentPane.registerKeyboardAction(e -> {
            dispose();
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        Map<String, String> pictureMapper = PictureMapper.INSTANCE.getPictureMapper();
        List<String> texts = new ArrayList<>();
        pictureMapper.forEach((s, s2) -> {
            texts.add(s + " → " + s2);
        });
        mapperList.setListData(texts.toArray(new String[0]));
        externalChooseBtn.addActionListener(actionEvent -> {
            FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFolderDescriptor(), event.getProject(), null, virtualFiles -> {
                if (virtualFiles == null || virtualFiles.isEmpty()) {
                    Messages.showErrorDialog("no pictures found in this folder", "");
                    return;
                }
                VirtualFile parentVf = virtualFiles.get(0);
                System.out.println(parentVf.getName());
                VirtualFile[] children = parentVf.getChildren();
                for (int j = 0; j < children.length; j++) {
                    // 找到图片文件
                    if (!children[j].isDirectory() && (children[j].getName().endsWith(".png")
                            || children[j].getName().endsWith(".jpg") ||
                            children[j].getName().endsWith(".bmp") ||
                            children[j].getName().endsWith(".svg") ||
                            children[j].getName().endsWith(".jpeg"))) {
                        externalFiles.add(children[j]);
                    }
                }
                if (externalFiles.isEmpty()) {
                    Messages.showErrorDialog(event.getProject(), "no pictures found in this folder", "");
                    return;
                }
                externalChooseBtn.setText(parentVf.getPath());
            });
        });

        VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(event.getDataContext());
        if (file != null) {
            String path = file.getPath();
            targetFolder = file;
            targetChooseBtn.setText(path);
        }
        targetChooseBtn.addActionListener(actionEvent -> {
            FileChooser.chooseFiles(FileChooserDescriptorFactory.createSingleFolderDescriptor(),
                    event.getProject(), targetFolder != null ? targetFolder : null, virtualFiles -> {
                        if (virtualFiles != null && !virtualFiles.isEmpty()) {
                            targetFolder = virtualFiles.get(0);
                            targetChooseBtn.setText(targetFolder.getPath());
                        }
                    });
        });
    }


    @Override
    protected void doOKAction() {
        if (externalFiles.isEmpty() || targetFolder == null) {
            Messages.showErrorDialog(project, "external folder or target folder is empty", "");
            return;
        }
        try {
            for (VirtualFile externalFile : externalFiles) {
                String target = PictureMapper.INSTANCE.getPictureMapper().get(externalFile.getName());
                if (target == null) {
                    continue;
                }
                String[] strings = target.split("/");
                String dir = strings[0];
                String name = strings[1];

                VirtualFile childDir = targetFolder.findChild(dir);
                if (childDir == null) {
                    childDir = targetFolder.createChildDirectory(this, dir);
                }
                VirtualFile existFile = childDir.findChild(name);
                if (existFile != null) {
                    existFile.delete(this);
                }
                externalFile.copy(this, childDir, name);

            }
        } catch (IOException e) {
            e.printStackTrace();
            Messages.showErrorDialog(project, "An unexpected error occurred, "+e.getMessage(), "");
            return;
        }
        Messages.showInfoMessage("pictures import successful!", "");
        dispose();
    }

    @Override
    protected @Nullable
    JComponent createCenterPanel() {
        setTitle("工具");
        setOKButtonText("导入");
        scrollPane.getViewport().setView(mapperList);
        return contentPane;
    }
}
