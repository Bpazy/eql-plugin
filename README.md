eql-plugin
===========
适用于Intellij IDEA的Eql插件

## Usage
生成Eql文件: `Generate code`(default: Windows: `Alt+insert`, Mac: `⌘N`)
![generateEqlFile](https://user-images.githubusercontent.com/9838749/32939543-c9b611f4-cbba-11e7-9254-6166ac47c3ae.gif)

跳转到Java或者Eql方法: `Quick fix`(default: Windows: `Alt + Enter`, Mac: `⌥↵`)  
> 这里应该有一张gif预览图。:smile:

## Development
当Gradle运行在Debug模式时，会自动下载合适的`Intellij IDEA Community`, `Intellij SDK`, `Source code`，并在当前目录下的`build`目录中生成沙箱数据，用于运行加载了插件的`IDEA Community`。  

第一次运行会下载约400Mb的数据，根据情况需要翻墙下载。

Bulid: `gradlew buildPlugin`  
Debug: `gradlew runIdea`
