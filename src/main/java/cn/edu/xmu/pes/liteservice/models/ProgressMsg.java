package cn.edu.xmu.pes.liteservice.models;

public class ProgressMsg {
    public int status; // 0 no task or terminate, 1 running or task done.
    public int filenum;
    public ProgressMsg(int status, int filenum) {
        this.status = status;
        this.filenum = filenum;
    }
}
