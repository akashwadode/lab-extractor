package queueengine_v1;

public class QueueReport {

    public int reportId;
    public int labId;
    public String filename;
    public String filepath;

    public QueueReport(int reportId, int labId,
                       String filename, String filepath) {

        this.reportId = reportId;
        this.labId = labId;
        this.filename = filename;
        this.filepath = filepath;
    }
}