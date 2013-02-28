package shyview;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

public class Picture implements IPicture {
	private final static int MAX_IMAGE_LOADING_TIME = 5;
	
	private URL picres;
	Future<Image> swapimage = null;
	String customname = null;
	
	public Picture(URL url) {
		picres = url;
	}
	public Picture(String strurl) throws MalformedURLException {
		picres = new URL(strurl);
	}
	public Picture(File f) throws MalformedURLException {
		picres = f.toURI().toURL();
	}

	@Override
	public String getName() {
		if (customname != null) return customname;
		return picres.getPath().substring(picres.getPath().lastIndexOf("/")+1);
	}

	@Override
	public String getPath() {
		return picres.toString();
	}

	@Override
	public Image getPicture() {
		ExecutorService exs = Executors.newSingleThreadExecutor();
		swapimage = exs.submit(new ImageLoadTask());
		try {
			return swapimage.get(MAX_IMAGE_LOADING_TIME, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	

	@Override
	public Dimension getDimension() {
		if (swapimage != null) {
			Dimension d = new Dimension();
			Image pic = getPicture();
			d.width = pic.getWidth(null);
			d.height = pic.getHeight(null);
			return d;
		}
		return null;
	}

	@Override
	public void preload() {
		getPicture();
	}

	@Override
	public void flush() {
		swapimage = null;
	}
	
	public void interrupt() {
		if (swapimage != null) swapimage.cancel(true);
	}
	
	class ImageLoadTask implements Callable<Image> {
		public Image call() throws Exception {
			if (swapimage.isDone()) return swapimage.get();
			try {
				return ImageIO.read(picres);
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	@Override
	public void setName(String name) {
		customname = name;
	}
}
