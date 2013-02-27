package shyview;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class shyNW {
	public shyNW() {}
	
	public boolean loadFile(String source, String destination) {
		java.io.BufferedInputStream in;
		try {
			in = new java.io.BufferedInputStream(new 
			java.net.URL(source).openStream());
			java.io.FileOutputStream fos = new java.io.FileOutputStream(destination);
			java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
			byte data[] = new byte[1024];
			while(in.read(data,0,1024)>=0) {
				bout.write(data);
			}
			bout.close();
			in.close();
			return true;
		} catch (MalformedURLException e) {return false;}
		catch (IOException e) {return false;}
	}
	
	public String postRequest(String destination, String[][] data) {
		try {
		    // Construct data
			String postdata = "";
			for (int i = 0; i < data.length; i++) {
				if (i != 0) postdata += "&";
				String[] keyvalue = data[i];
				postdata += URLEncoder.encode(keyvalue[0], "UTF-8") + "=" + URLEncoder.encode(keyvalue[1], "UTF-8");
			}

		    //data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" + URLEncoder.encode("value2", "UTF-8");

		    // Send data
		    URL url = new URL(destination);
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(postdata);
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    String re = "";
		    while ((line = rd.readLine()) != null) {re += line;}
		    wr.close();
		    rd.close();
		    return re;
		} catch (Exception e) {return "";}
	}
	public void loadBinary(String source, String destination) {
		try {
		URL u = new URL(source);
		
	    URLConnection uc = u.openConnection();
	    String contentType = uc.getContentType();
	    int contentLength = uc.getContentLength();
	    if (contentType.startsWith("text/") || contentLength == -1) {
	      throw new IOException("This is not a binary file.");
	    }
	    InputStream raw = uc.getInputStream();
	    InputStream in = new BufferedInputStream(raw);
	    byte[] data = new byte[contentLength];
	    int bytesRead = 0;
	    int offset = 0;
	    while (offset < contentLength) {
	      bytesRead = in.read(data, offset, data.length - offset);
	      if (bytesRead == -1)
	        break;
	      offset += bytesRead;
	    }
	    in.close();

	    if (offset != contentLength) {
	      throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
	    }

	    String filename = destination;
	    FileOutputStream out = new FileOutputStream(filename);
	    out.write(data);
	    out.flush();
	    out.close();
	    } catch (MalformedURLException e) {e.printStackTrace();} catch (FileNotFoundException e) {e.printStackTrace();} catch (IOException e) {e.printStackTrace();}
	}
}
