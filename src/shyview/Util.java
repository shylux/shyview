package shyview;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.swing.JFrame;

public class Util {
	static boolean validURL(URL url) {
		try {
		    URLConnection conn = url.openConnection();
		    conn.connect();
		} catch (MalformedURLException e) {
		    return false;
		} catch (IOException e) {
		    return false;
		}
		return true;
	}
	static boolean validURL(String strurl) {
		try {
			URL url = new URL(strurl);
			return validURL(url);
		} catch (MalformedURLException e) {
			return false;
		}
	}
	//Check URL before converting!
	static URL getURL(String strurl) {
		try {
			return new URL(strurl);
		} catch (MalformedURLException e) {
			return null;
		}
	}
	
	static void setFullscreen(JFrame window, boolean tofullscreen) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device;
		device = ge.getDefaultScreenDevice();
		
		if (device.isFullScreenSupported()) {
			if (tofullscreen) {
				System.out.println("Set");
				device.setFullScreenWindow(window);
			} else {
				System.out.println("Unset");
				device.setFullScreenWindow(null);
			}
		}
	}
}
