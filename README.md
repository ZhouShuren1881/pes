# PES - Public Exam Server

## 介绍
PES 是一个代码自动化测试服务器，允许用户自己提交测试参数.

## 构建信息

编译 : `通过`

功能测试 : `通过`

运行环境 : `Linux Only`

## 使用教程
### 单机运行
1. 在Linux下使用
1. 将Example_script下的pesserver文件夹拷贝到linux用户根目录
1. 运行服务器后访问：
    * localhost:8899 用户界面
    * localhost:8899/superadmin.html 管理员界面
1. 在`application.properties`中可以配置一些属性
1. 支持重写shell脚本以调用更多Linux程序，程序输出到`$1`文件夹下的文件都可以被普通用户访问
### 集群
1. 使用nfs将主服务器磁盘挂载到从服务器同名目录下
1. 其他操作同上
