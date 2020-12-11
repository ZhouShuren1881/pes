# PES - Public Exam Server

## Introduce
PES is a server for Code Test.

## Notice

Build : `PASS`

Use For Production : `YES`

Run Environment : `Linux Only`

## How To Use
1. 在Linux下使用
1. 将Example_script下的pesserver文件夹拷贝到linux用户根目录
1. 直接运行服务器
    * localhost:8899 用户界面
    * localhost:8899/superadmin.html 管理员界面
1. 在`application.properties`中可以配置一些属性
1. 支持重写shell脚本以调用更多Linux程序，程序输出到`$1`文件夹下的文件都可以被普通用户访问
