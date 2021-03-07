package space.stdx.rockssandbox;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class DocumentRecord implements Serializable {

    private static final long serialVersionUID = -2131632934122501664L;

    private String title;
    private final URL url;
    private Date lastModificationDate;
    private int pageSize;
    private TreeMap<String, Integer> freqTable;
    private ArrayList<URL> childLinks;

    public DocumentRecord(URL url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public DocumentRecord setTitle(String title) {
        this.title = title;
        return this;
    }

    public URL getUrl() {
        return url;
    }

    public Date getLastModificationDate() {
        return lastModificationDate;
    }

    public DocumentRecord setLastModificationDate(Date lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    public int getPageSize() {
        return pageSize;
    }

    public DocumentRecord setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }

    public TreeMap<String, Integer> getFreqTable() {
        return freqTable;
    }

    public DocumentRecord setFreqTable(TreeMap<String, Integer> freqTable) {
        this.freqTable = freqTable;
        return this;
    }

    public ArrayList<URL> getChildLinks() {
        return childLinks;
    }

    public DocumentRecord setChildLinks(ArrayList<URL> childLinks) {
        this.childLinks = childLinks;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(title).append('\n');
        builder.append(url).append('\n');
        builder.append(lastModificationDate).append(", ").append(pageSize).append('\n');
        for (Map.Entry<String, Integer> e : freqTable.entrySet()) {
            builder.append(e.getKey()).append(' ').append(e.getValue()).append(';');
        }
        builder.append('\n');
        for (URL childLink : childLinks) {
            builder.append(childLink).append('\n');
        }
        return builder.toString();
    }
}
