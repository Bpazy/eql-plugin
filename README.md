eql-plugin
===========
适用于 Intellij IDEA 的 [bingoohuang/eql](https://github.com/bingoohuang/eql) 插件。

## Install
Install eql-plugin in `Intellij IDEA` or https://plugins.jetbrains.com/plugin/10169-eql-plugin

## Usage
* 生成 eql 文件: `Generate code`(default: Windows: `Alt+insert`, Mac: `⌘N`)

![generateEqlFile](https://user-images.githubusercontent.com/9838749/32939543-c9b611f4-cbba-11e7-9254-6166ac47c3ae.gif)

* 跳转到 Java 或者 eql 方法: `Quick fix`(default: Windows: `Alt + Enter`, Mac: `⌥↵`)  

![jump](https://user-images.githubusercontent.com/9838749/33000817-41a6e2d4-cde5-11e7-9c42-c436042d7bf9.gif)

## Development
当 Gradle 运行在 Debug 模式时，会自动下载合适的 `Intellij IDEA Community`, `Intellij SDK`, `Source code`，并在当前目录下的 `build` 目录中生成沙箱数据，用于运行加载了插件的 `IDEA Community`。  

第一次运行会下载约 400Mb 的数据，根据情况需要翻墙下载。

Bulid: `gradlew buildPlugin`  
Debug: `gradlew runIdea`
