package cn.edu.xmu.pes.liteservice.models;

public class ProgressMsg {
    public int status; // 0 no task, 1 running or task done or terminate.
    public int filenum;
    public ProgressMsg(int status, int filenum) {
        this.status = status;
        this.filenum = filenum;
    }
}
