## Android Studio Plugin/Intellij IDEA Plugin插件开发入门，开发属于自己的第一款插件

## 前置知识
> Android Studio是基于Intellij IDEA开发的，所以需要使用IDEA开发插件，同时，开发出的插件可以适用于所有基于Jetbrain Intellij的系列产品，除非插件使用了特定平台的依赖库或者限定了适用的平台

## 功能需求
> 对安卓工程下的各个res/mipmap内的不同大小的logo图片进行一键替换

![微信截图_20210712165128.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/445c20eef18b4a52b6687f4b57cadf16~tplv-k3u1fbpfcp-watermark.image)

## 仓库地址
[GitHub](https://github.com/Joehaivo/first-plugin)

## 1. 初始化工程
1. 打开IntelliJ IDEA Ultimate （我使用的是2021.1.3），点击New Project
2. 左侧选择Gradle类型（不再推荐左侧选择Intellij Platform Plugin类型），右侧指定JDK版本为1.8，勾选Java和Intellij Platform Plugin，点击Next

![微信截图_20210712160041.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/93d7f9d3ad9d4713ad1ef22a33c838fa~tplv-k3u1fbpfcp-watermark.image)
3. 输入工程名first-plugin，点击Finish

## 2. 修改配置信息
### 1. 修改build.gradle文件
```gradle
intellij {
    version = '2020.1' // 因为Android Studio 4.1是基于IDEA 2020.1 Community版本开发的，所以这里调试也指定为此版本
    plugins = ['android']
}

buildSearchableOptions {
    enabled = false
}

patchPluginXml {
    changeNotes = """
      Add change notes here.<br>
      <em>most HTML tags may be used</em>"""
    sinceBuild = '191' // 插件适用的IDEA版本范围，此范围基本涵盖了Android Studio最近两三年的版本
    untilBuild = '212.*'
}
```
### 2. 修改plugin.xml文件
- 按需修改\<name>、\<vendor>、\<description>标签内的内容
- 在\<depends>标签下新增：
```xml
<depends>com.intellij.modules.platform</depends>
<depends>com.intellij.modules.xml</depends> // 支持xml文件操作
<depends>org.jetbrains.android</depends> // AS相关
```
### 3. 创建包结构
在/src/main/java文件夹上右键，New package创建com.haivo.plugin包

## 2. 同步工程
点击Gradle Sync同步工程, 第一次同步需要下载IDEA-IC2020.1版本以及android依赖，可能需要10-20分钟，耐心等待同步完成。

## 3. 创建菜单项（Action）
1. 在com.haivo.plugin文件夹上右键，选择New > Plugin Devkit > Action

![微信截图_20210712160530.png](https://p6-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/7435282612354fcbb5033b14d7e7aa88~tplv-k3u1fbpfcp-watermark.image)

2. 依次填入id name等信息，Action ID最好采用全限定名，Group选择ProjectViewPopupMenu，表示将此菜单附加到右键菜单项内，你也可以选择ToolsMenu，表示附加到顶部Tools选项菜单内
   ![微信截图_20210712135832.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/6b68314ff22c4d7e9590cbacfb55a87c~tplv-k3u1fbpfcp-watermark.image)

3. 另一种创建Action的方式是直接编辑plugin.xml的\<actions>标签，并创建相应的class，这种方式还可以灵活的为Action创建一组ActionGroup，效果就是菜单项会出现“▷”可展开项
```xml
<actions>
        <!-- Add your actions here -->
        <action id="com.haivo.plugin.importpictureaction" class="ImportPictureAction" text="导入图片"
                description="一键批量导入图片">
            <add-to-group group-id="ProjectViewPopupMenu" anchor="last"/>
        </action>
</actions>
```

## 4. 创建对话框（Dialog）
1. 在com.haivo.plugin文件夹上右键，选择New > Swing UI Designer > Create Dialog Class
2. 输入类名，取消三个勾选项，点击OK
   ![微信截图_20210712140024.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/86e11345667b44f4b6d95d3df173c109~tplv-k3u1fbpfcp-watermark.image)

## 5. 实现UI界面
1. 从这里开始就是JavaFx的知识了，不过不用了解太深，我们修改ImportPictureDialog类的父类为DialogWrapper，实现createCenterPanel()方法，修改构造方法，并添加init()方法（重点，否则没有界面！）
```java
public class ImportPictureDialog extends DialogWrapper {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    public ImportPictureDialog(AnActionEvent event) {
        super(event.getProject());
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return contentPane;
    }
}
```
2. 打开ImportPictureDialog.form文件，是不是很熟悉？其实这跟AS中的layout editor很像，
   选中ok按钮按Delete删除，cancel按钮同理，因为DialogWrapper自带了这两个按钮

3. 从右侧组件库依次拖动JLabel、JTextField到框里，并修改文本与布局
   ![微信截图_20210712144123.png](https://p9-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/fb522feb55a14eb6842ab36d98724dfa~tplv-k3u1fbpfcp-watermark.image)

## 6. 业务实现 & 调试
1. 修改ImportPictureAction
```java
public class ImportPictureAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // 此处的代码会在Action被点击后执行
        new ImportPictureDialog(e).show();
    }
}
```
2. 完成后我们先调试一波，点击Run Plugin(▶)按钮，稍等一会儿会启动一个新的IDEA窗口实例，这个实例会自动安装好我们的插件，我们选择一个安卓工程打开，然后在左侧Project窗口内右键app/src/res文件夹，会发现菜单的底部出现了一个“导入图片”选项，点击出现下述图片，至此我们的插件已基本成型，接下来只需要编写业务代码即可


![微信截图_20210712144910.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/816696d472c3466da6f6bdc0852e829d~tplv-k3u1fbpfcp-watermark.image)

3. 具体的业务代码不再此处列出了，查看源码即可，主要都在ImportPictureDialog.java类里

## 7. 插件打包 & 发布
1. 在右侧点击Gradle，找到publishPlugin，双击执行，或者在命令行下执行
```shell
.\gradlew publishPlugin
```
2. 在build/libs下即可找到该jar包

![微信截图_20210712161236.png](https://p3-juejin.byteimg.com/tos-cn-i-k3u1fbpfcp/23d6cfbc46274c25abd17642a21a620f~tplv-k3u1fbpfcp-watermark.image)

3. 将该jar包拖进Android Studio的编辑区， 然后重启AS，插件即安装完成