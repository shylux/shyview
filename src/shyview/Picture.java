package shyview;

import java.awt.Dimension;
import java.awt.Image;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.imageio.ImageIO;

public class Picture implements IPicture {
	private final static int MAX_IMAGE_LOADING_TIME = 5; // seconds
	
	private URL picres;
	Future<Image> swapimage = null;
	String alternative_name = null;
	
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
		if (alternative_name != null) return alternative_name;
		return picres.getPath().substring(picres.getPath().lastIndexOf("/")+1);
	}

	@Override
	public String getPath() {
		return picres.toString();
	}

	@Override
	public Image getPicture() throws StillLoadingException, FileNotFoundException {
		if (swapimage == null) preload();
		if (!swapimage.isDone()) throw new StillLoadingException();
		Image tmp;
		try {
			tmp = swapimage.get();
		} catch (Exception e) {e.printStackTrace(); throw new FileNotFoundException();}
		if (tmp == null) throw new FileNotFoundException();
		return tmp;
	}
	

	@Override
	public Dimension getDimension() {
		if (swapimage != null) {
			Dimension d = new Dimension();
			Image pic;
			try {
			pic = getPicture();
			} catch(Exception e) {return null;}
			d.width = pic.getWidth(null);
			d.height = pic.getHeight(null);
			return d;
		}
		return null;
	}

	@Override
	public void flush() {
		swapimage = null;
	}
	
	public void interrupt() {
		if (swapimage != null) swapimage.cancel(true);
	}
	
	public void preload() {
		//System.out.println("Preloading "+getName());
		ExecutorService exs = Executors.newSingleThreadExecutor();
		swapimage = exs.submit(new ImageLoadTask()); // start task
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

	class StillLoadingException extends IOException {
		private static final long serialVersionUID = 1L;
	}

	@Override
	public void setName(String name) {
		alternative_name = name;
	}
}
