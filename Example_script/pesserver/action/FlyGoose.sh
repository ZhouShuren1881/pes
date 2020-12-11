# FlyGoose.sh在用户的任务文件夹下运行，并将结果保存在该文件夹下
## 并发执行脚本
## 如需多台服务器进行测试，可以mount主服务器的文件夹来同步文件

# 使用sed 去除Linux 终端颜色代码，副作用：只有进程执行完毕才会输出到文件中
setsid ~/pesserver/action/lsGoose.sh $1 $2 $3 2>&1 | sed "s,\\x1B\\[[0-9;]*[a-zA-Z],,g" >$1/report_mvn.txt 2>&1 &

# 在运行上面命令的同时，调用另一个耗时100秒程序
setsid ~/pesserver/action/LandGoose.sh 100 2>&1 >$1/report_land.txt 2>&1 &
