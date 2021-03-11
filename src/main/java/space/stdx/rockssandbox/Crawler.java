package space.stdx.rockssandbox;

import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import org.jsoup.HttpStatusException;

import javax.net.ssl.SSLHandshakeException;
import javax.print.Doc;
import java.lang.RuntimeException;
import java.util.stream.Collectors;

/** The data structure for the crawling queue.
 */
class Link{
	String url;
	int level;
	Link (String url, int level) {  
	    this.url = url;
	    this.level = level; 
	}  
}

@SuppressWarnings("serial")
/** This is customized exception for those pages that have been visited before.
 */
class RevisitException 
	extends RuntimeException {
	public RevisitException() {
	    super();
	}
}

public class Crawler {
	private HashSet<String> urls;     // the set of urls that have been visited before
	public Vector<Link> todos; // the queue of URLs to be crawled
	private int max_crawl_depth = 100;  // feel free to change the depth limit of the spider.
	private int counter = 0; //\ to count the number of retrieved pages
	static List<DocumentRecord> dr = new ArrayList<>();

	static final int MAX_NUMBER_PAGES = 30; // max page
	
	Crawler(String _url) {
		this.todos = new Vector<Link>();
		this.todos.add(new Link(_url, 1));
		this.urls = new HashSet<String>();
	}
	
	/**
	 * Send an HTTP request and analyze the response.
	 * @return {Response} res
	 * @throws HttpStatusException for non-existing pages
	 * @throws IOException
	 */
	public Response getResponse(String url) throws HttpStatusException, IOException {
		if (this.urls.contains(url)) {
			throw new RevisitException(); // if the page has been visited, break the function
		 }


		Connection conn = null;

		conn = Jsoup.connect(url).followRedirects(false);
		// the default body size is 2Mb, to attain unlimited page, use the following.
		// Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
		Response res;
		try {
			/* establish the connection and retrieve the response */
			 res = conn.execute();
			 /* if the link redirects to other place... */
			 if(res.hasHeader("location")) {
				 String actual_url = res.header("location");
				 if (this.urls.contains(actual_url)) {
				 	 throw new RevisitException();
				 }
				 else {
					 this.urls.add(actual_url);
				 }
			 }
			 else {
				 this.urls.add(url);
			 }
		} catch (HttpStatusException e) {
			throw e;
		}

		/* Get the metadata from the result */
//		String lastModified = res.header("last-modified");
//		int size = res.bodyAsBytes().length;
//		String htmlLang = res.parse().select("html").first().attr("lang");
//		String bodyLang = res.parse().select("body").first().attr("lang");
//		String lang = htmlLang + bodyLang;
//		System.out.printf("Last Modified: %s\n", lastModified);
//		System.out.printf("Size: %d Bytes\n", size);
//		System.out.printf("Language: %s\n", lang);
		return res;
	}
	
	/** Extract words in the web page content.
	 * note: use StringTokenizer to tokenize the result
	 * @param {Document} doc
	 * @return {Vector<String>} a list of words in the web page body
	 */
	public Vector<String> extractWords(Document doc) {
		 Vector<String> result = new Vector<String>();
		// ADD YOUR CODES HERE
		 String contents = doc.body().text();
	     StringTokenizer st = new StringTokenizer(contents);
	     while (st.hasMoreTokens()) {
            result.add(st.nextToken());
	     }
	     return result;		
	}
	
	/** Extract useful external urls on the web page.
	 * note: filter out images, emails, etc.
	 * @param {Document} doc
	 * @return {Vector<String>} a list of external links on the web page
	 */
	public Vector<String> extractLinks(Document doc) {
		Vector<String> result = new Vector<String>();
		// ADD YOUR CODES HERE
        Elements links = doc.select("a[href]");
        for (Element link: links) {
        	String linkString = link.attr("href");
        	// filter out false link
			if (!filterUrl(linkString)) {
//				System.out.printf("linkString: %s\n", linkString);
				continue;
			}
            result.add(linkString);
        }
        return result;
	}
	
	
	/** Use a queue to manage crawl tasks.
	 */
	public void crawlLoop() {
		while(!this.todos.isEmpty()) {
			Link focus = this.todos.remove(0);
			if (focus.level > this.max_crawl_depth) break; // stop criteria
			if (this.urls.contains(focus.url)) continue;   // ignore pages that has been visited
			if (this.counter >= MAX_NUMBER_PAGES) {  // stop when number of pages exceed the constant
				break;
			} else {
				counter++;
			}

			/* start to crawl on the page */
			try {
				Response returns = this.getResponse(focus.url);
				Response res = returns.bufferUp();
				Document doc = res.parse();

				// Check lang
				String htmlLang = doc.select("html").first().attr("lang");
				String bodyLang = doc.select("body").first().attr("lang");
				String lang = htmlLang + bodyLang;
				if (!lang.toLowerCase().contains("en"))
					continue;
				// Check lang end
				
				Vector<String> words = this.extractWords(doc);		
				System.out.println("\nWords:");
				for(String word: words)
					System.out.print(word + ", ");
		
				Vector<String> links = this.extractLinks(doc);
//				System.out.printf("\n\nLinks:");
				for(int i=0;i<links.size();++i) {
					String link = links.get(i);
					link = urlPreprocess(focus.url, link);
					links.set(i, link);
//					System.out.println("link: "+link);
					this.todos.add(new Link(link, focus.level + 1)); // add links
				}

				// retrieving data
				String lastModified = res.header("Last-Modified");
				if (lastModified==null || lastModified.equals("")){
					lastModified = res.header("Date");
				}
				int size = res.bodyAsBytes().length;

				// count keywords
				TreeMap<String, Integer> freqTable = new TreeMap<>();
				for (String item : words)
					freqTable.put(item, freqTable.getOrDefault(item, 0) + 1);



				// Calling document record to serialise the retrieved data
				DocumentRecord documentRecord = new DocumentRecord(new URL(focus.url));
				documentRecord.setTitle(res.parse().title());
				documentRecord.setLastModificationDate(new Date(lastModified));
				documentRecord.setFreqTable(freqTable);

				ArrayList<URL> linksList = links.stream().map(a->{
						URL url;
						try {
							url = new URL(a);
						} catch (MalformedURLException e){
							System.out.println("exception: "+ a);
							return null;
						}
						return url;
					}).collect(Collectors.toCollection(ArrayList::new));
				documentRecord.setChildLinks(linksList);

				dr.add(documentRecord);


			} catch (HttpStatusException e) {
	            // e.printStackTrace ();
				System.out.printf("\nLink Error: %s\n", focus.url);
	    	} catch(SSLHandshakeException e){
				System.out.printf("\nSSL Error: %s\n", focus.url);
			} catch (IOException e) {
	    		e.printStackTrace();
	    	}  catch (RevisitException e) {
	    	}
		}
		
	}

	/**
	 * To process relative url to absolute url.
	 * If it is absolute path, i.e., contains 'http' keyword, returns url.
	 * currentUrl and url shall not be empty or null
	 * @param currentUrl the url of the page it is processing at
	 * @param url the url retrieved in the current url
	 * @return processed URL
	 */
	private static String urlPreprocess(String currentUrl, String url){
		if (url.contains("http"))
			return url;
		else {
			if (currentUrl.endsWith("/") && url.startsWith("/"))
				currentUrl = currentUrl.substring(0, currentUrl.length()-1);
			return currentUrl + url;
		}
	}

	/**
	 * To filter out unwanted junk links
	 * return false if the link is unwanted
	 * @param linkString the link to be processed
	 * @return if the link is wanted
	 */
	private boolean filterUrl(String linkString){
		if (linkString.trim().isEmpty()){
			return false;
		} else if (linkString.contains("mailto:")) {
			return false;
		} else if (linkString.contains("javascript")) {
			return false;
		} else if (linkString.charAt(0) == '#'){
			return false;
		}

		return true;
	}
	
	public static void main (String[] args) {
		String url = "https://www.cse.ust.hk/";
		Crawler crawler = new Crawler(url);
		crawler.crawlLoop();
		System.out.println("\nSuccessfully Returned");

		System.out.println("\n-------------document records printing------------------");
		for (DocumentRecord i : dr){
			System.out.println(i.toString());
		}
		System.out.println("---------------document records printing finished---------------");
	}
}
	
