2019.10.30
使用JAVA解析markdown笔记生成HTML文件

参考github项目

使用 java 实现一个简单的 markdown 语法解析器:
https://github.com/libaoquan95/MarkDownParser

对该项目进行了简单的改进和复用：

1.新增<meta charset=\"utf-8\">,可解析中文笔记

2.引入BootStrap和jQuery，可直接加入所需的jQuery代码

3.修复了html获取的标题，现在获取更加准确

4.使用switch/equal等语法简化了代码

5.修复了识别图片名错误的问题

6.修复了无法识别链接的问题

使用方法：

```java
MarkDownParseUtil md = new MarkDownParseUtil("src\\文件名.md");
md.createHtmlFile();
```

可通过修改createHtmlFile(String fileName)方法以适应项目需求

2019.11.1
更改了代码对于链接的处理，现在的链接会正确的生成新页面而非更改原页面。
