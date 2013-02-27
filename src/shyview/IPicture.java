package shyview;

import java.awt.Dimension;
import java.awt.Image;

public interface IPicture {
	public void setName(String name);
	public String getName();
	public String getPath();
	public Image getPicture();
	public void interrupt();
	public Dimension getDimension();
	/**
	 * Loads picture in cache.
	 */
	public void preload();
	/**
	 * Removes picture from cache to save memory.
	 */
	public void flush();
}
