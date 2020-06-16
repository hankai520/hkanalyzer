HkAnalyzer 中文分词器
=====================

 

一个基于字符串匹配的中文分词工具，支持lucene6。

本工具代码源于
IKAnalyzer。IKAnalyzer的作者为林良益（<linliangyi2007@gmail.com>），项目源代码位于
<http://code.google.com/p/ik-analyzer/>

 

本工程改动如下：

1.  适配Lucene 6

2.  调整代码风格，增加必要注释，增强可读性

3.  增加接口扩展了词源，可以通过接口增加分词（ren.hankai.cnanalyzer.core.Dictionary）

4.  增加索引、检索、分词示例代码，增加易用性（ren.hankai.cnanalyzer.sample）
