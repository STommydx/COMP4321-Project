package hk.ust.cse.comp4321.proj1;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.*;

public class DocumentRecord implements Serializable {

    private static final long serialVersionUID = -2131632934122501664L;

    private String title = "";
    private final URL url;
    private Date lastModificationDate = new Date();
    private int pageSize;
    private TreeMap<String, Integer> freqTable = new TreeMap<>();
    private ArrayList<URL> childLinks = new ArrayList<>();
    private Vector<String> words; // does not store into DB, delete before putting into db

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

    public void setWords(Vector<String> words){
        this.words = words;
    }

    private Vector<String> getWordsAndSetNull(){
        Vector<String> words = this.words;
        this.words = null;
        return words;
    }

    public TreeMap<String, ArrayList<Integer>> getWordsWithLoc(){
        Vector<String> words = this.getWordsAndSetNull();
        TreeMap<String, ArrayList<Integer>> result = new TreeMap<>();
        for (int i=0; i<words.size(); ++i){
            ArrayList<Integer> locations = result.get(words.get(i));
            if (locations == null)
                locations = new ArrayList<>();
            locations.add(i); // won't repeat as iterator i only appears once for each value
            System.out.printf("Putting %s\n", words.get(i));
            result.put(words.get(i), locations);
        }

        return result;
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
        childLinks.stream().map(URL::toString).sorted().forEach(childLink -> builder.append(childLink).append('\n'));
        return builder.toString();
    }
}
