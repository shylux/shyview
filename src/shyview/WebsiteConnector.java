package shyview;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
//simple website abfragen k�nnen damit einfach realisiert werden
public class WebsiteConnector {
	private URL selected_url;
	public WebsiteConnector(String url) {
		try {
			this.selected_url = new URL(url);
		} catch (MalformedURLException e) {e.printStackTrace();}
	}

	public void setURL(URL newurl) {
		this.selected_url = newurl;
	}
	
	public String getHTML() throws IOException {
		URLConnection urlc = this.selected_url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String templine, returnValue = "";
		while ((templine = in.readLine()) != null) returnValue += templine;
		in.close();
		return returnValue;
	}
	
	public String getImagefromURL() {
		String img = null;
		
		String imgdata;
		try {
			imgdata = getHTML();
			img = imgdata;
		} catch (IOException e) {e.printStackTrace();}
		return img;
	}
	
	
	public String getHTML_GET(Boolean docontinue) throws IOException {
		URLConnection urlc = this.selected_url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(urlc.getInputStream()));
		String templine, returnValue = "";
		while ((templine = in.readLine()) != null) returnValue += templine;
		in.close();
		return returnValue;
	}
	
	
	public String getHTML_POST(HashMap<?, ?> data) throws IOException {
		return getHTML_POST(data, false);
	}
	public String getHTML_POST(HashMap<?, ?> data, Boolean docontinue) throws IOException {
		//Parse POST-data
		String POST_data = "", returnValue = "", templine;
		Set<?> set = data.entrySet();
		Iterator<?> i = set.iterator();
		while (i.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry me = (Entry<?, ?>) i.next();
			try {
				if (POST_data.length() != 0) POST_data += "&";
				POST_data += URLEncoder.encode(me.getKey().toString(), "UTF-8") + "=" + URLEncoder.encode(me.getValue().toString(), "UTF-8");
			} catch (UnsupportedEncodingException e) {e.printStackTrace();} 
		}
		
		//Send Request
		URLConnection urlconn = this.selected_url.openConnection();
		urlconn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(urlconn.getOutputStream());
		wr.write(POST_data);
		wr.flush();
		
		//Get the response
		BufferedReader in = new BufferedReader(new InputStreamReader(urlconn.getInputStream()));
		while ((templine = in.readLine()) != null) {
			returnValue += templine;
		}
		wr.close();
		in.close();
		
		System.out.println(POST_data);
		return returnValue;
	}
}
