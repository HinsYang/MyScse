# 模拟登陆华软信息系统+JSoup解析课程表
## 思路
1. URLConnection获取Cookie和登录表单hidden域
2. URLConnection把所需参数输出到服务器模拟登录
3. JSoup解析出想要的数据

## 使用说明
1. 使用开发工具，打开bin/MyScse.class文件，修改用户名和密码，或者在Program Param里使用 -u <用户名> -p <密码> 参数指定用户名密码后运行。
 
2. 直接使用编译文件，bin/MyScse.class文件，复制到任意目录中，打开命令行，进入到该目录。请使用 -u <用户名> -p <密码> 参数指定用户名密码，命令如下：
 ```
 java MyScse -u 用户名 -p 密码
 ```

## Bug Report
Email: 842328916@qq.com