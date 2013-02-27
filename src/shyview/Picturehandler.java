package shyview;

import java.awt.AlphaComposite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Transparency;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.ImageObserver;
import java.awt.image.Kernel;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;

import shyview.MangaWatcherDB.MangaMenuItem;
import webmate.IWebMateListener;
import MenuScroller.MenuScroller;
@SuppressWarnings("serial")
public class Picturehandler extends JPanel implements ImageObserver, ActionListener, DropTargetListener, IWebMateListener {
	private ArrayList<IPicList>  listlist = new ArrayList<IPicList>();
	private ShyluxFileFilter picturefilter = new ShyluxFileFilter();
	private ShyluxFileFilter jsonfilter = new ShyluxFileFilter();
	private IPicList mylist = new PictureList("Default");
	private JMenu menuLists;
	private JMenu menuDB;
	private MangaWatcherDB mangaDB;
	private String DBLocation = "";
	private Timer timer = new Timer(1000, this);
	private Image defaultimage = new ImageIcon(getClass().getResource("DefaultImage.gif")).getImage();
	private Image errorimage = new ImageIcon(getClass().getResource("ErrorImage.jpg")).getImage();
	private int index = 0;
	private PicViewGUI parent;
	private IPicInfo info;
	private File favorite_folder = null;
	private Preferences pref;
	
	Picturehandler(JMenu listcontainer, PicViewGUI parent) {
		this.info = new TitleInformer(parent);
		this.parent = parent;
		this.menuLists = listcontainer;
		//this.menuDB = mnuDB;
		if (this.menuDB != null) {
			this.menuDB.addMouseListener(new DBMenuListener());
			new DropTarget(menuDB, this);
		}
		listlist.add(mylist);
		this.picturefilter.addExtension(".jpg");
		this.picturefilter.addExtension(".png");
		this.picturefilter.addExtension(".gif");
		this.picturefilter.addExtension(".jpeg");
		this.picturefilter.addExtension(".bmp");
		this.picturefilter.addExtension(".img");

	    this.jsonfilter.addExtension(".json");
		repaint();
		this.redrawlists();
		new DropTarget(this, this);
		
		pref = Preferences.userNodeForPackage(getClass());

		int timer_delay = pref.getInt("timer_delay", 300);
		System.out.println(timer_delay);
		if (timer_delay > 0) this.setTimerdelay(timer_delay);
	}
	
	public IPicture acpic() {
		if (mylist.size() == 0 && this.count() > 0) return this.getNextList().current();
		return mylist.current();
	}
	public IPicList aclist() {
		return mylist;
	}
	
	public void addPicture(String Path, String list) {
		try {
			this.getList(list).add(new WebPicture(Path));
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.repaint();
		//this.image = newpic.getPicture();
	}
	public void addPicture(IPicture pic, String list) {
		this.getList(list).add(pic);
		this.repaint();
	}
	public void getNext() {
		if (mylist != null) {
			if (acpic() != null) acpic().flush();
			if (mylist.next() == null) getNextList();
		}
		info.update(aclist());
		repaint();
	}
	public void getPrevious() {
		if (mylist != null) {
			if (acpic() != null) acpic().flush();
			if (mylist.previous() == null) this.getPreviousList();
		}
		info.update(aclist());
		repaint();
	}
	public IPicList getNextList() {
		if (this.listlist.size() == 0) return new PictureList("Default");
		index += 1;
		if (listlist.size() - 1 < index) index = 0;
		this.mylist = listlist.get(index);
		this.mylist.setIndex(0);
		repaint();
		return this.mylist;
	}
	public IPicList getPreviousList() {
		if (this.listlist.size() == 0) return new PictureList("Default");
		index -= 1;
		if (index < 0) index = listlist.size() - 1;
		this.mylist = listlist.get(index);
		this.mylist.setIndex(0);
		info.update(aclist());
		repaint();
		return this.mylist;
	}
	public void setList(String listname) {
		//for (List list: this.listlist) {
		for (int i = 0; i < this.listlist.size(); i++) {
			IPicList list = this.listlist.get(i);
			if (list.getName() == listname) { 
				acpic().flush();
				this.mylist = list;
				this.index = i;
				mylist.setIndex(0);
			}
		}
	}
	public void setList(IPicList newlist) {
		this.listlist.add(newlist);
		this.mylist = newlist;
		mylist.setIndex(0);
	}
	public void setList(int index) {
		if (index > this.listlist.size()) index = 0;
		this.index = index;
		this.mylist = this.listlist.get(index);
		this.mylist.setIndex(0);
	}
	public synchronized void addLists(List<IPicList> lists) {
		listlist.addAll(lists);
		redrawlists();
	}
	
	public void setInformer(IPicInfo info) {
		this.info = info;
	}
	
	public boolean imageUpdate( ImageObserver imageObserver ) {
		repaint();
		return true;
	}
	public Dimension getPreferredSize() {
		return new Dimension(acpic().getDimension());
	}
	public int count() {
		int value = 0;
		for (IPicList list: this.listlist) {
			value += list.size();
		}
		return value;
	}
	
	public void setTimerdelay(int delay) {
		pref.putInt("timer_delay", delay);
		this.timer.setDelay(delay);
	}
	public int getTimerdelay() {
		return this.timer.getDelay();
	}
	public void startTimer() {
		this.timer.start();
	}
	public void stopTimer() {
		this.timer.stop();
	}
	public void restartTimer() {
		this.timer.restart();
	}
	public void toggleTimer() {
		if (this.timer.isRunning()) {
			this.timer.stop();
		} else {
			this.timer.start();
		}
	}
	public boolean isTimerRunning() {
		return this.timer.isRunning();
	}
	
	public IPicList getList(String listname) {
		for (IPicList list: this.listlist) {
			String listename = list.getName();
			if (listename.equals(listname)) return list;
		}
		IPicList newlist = new PictureList(listname);		
		this.listlist.add(newlist);	
		return newlist;
	}
	
	public int blur = 0;
	public void paint(Graphics g) {
		super.paint(g);
		
		
		this.info.update(aclist());

		//bei resize nur umrisse anzeigen
		/*if (this.isonresize || this.lastwidth != this.getWidth() || this.lastheight != this.getHeight()) {
			System.out.println("bol:" + this.isonresize + " : lastw " + this.lastwidth + " acw " + this.getWidth());
			return;
		}*/
		
	
		int position[] = new int[2];
		position[0] = 1;
		position[1] = 1;
		Image image;

		//Load image
		this.info.pushProcess("Loading picture");
		try {
			while ((image = acpic().getPicture()) == null) {
				System.err.println("Removed "+acpic().getName());
				mylist.remove(acpic());
				mylist.remove(mylist.getIndex());
			}
			if (mylist.size() > 0)	{
			} else {
				this.info.clear();
			}
		} catch (Exception e) {
			if (this.errorimage == null) return;
			image = this.errorimage;
			if (this.count() == 0) image = this.defaultimage;
		}
		//this.info.setVisible(false);
		
		if (image == null) {
			this.aclist().remove(acpic());
			this.repaint();
			return;
		}
		
		//Wenn image nicht geladen werden kann
		if (image.getWidth(this) <= 0 || image.getHeight(this) <= 0) {
			image = this.errorimage;
			System.err.println("Can't load picture");
		}
		
		this.info.pushProcess("Processing");
		
		Graphics2D g2 = (Graphics2D)g;
		BufferedImage output = Picturehandler.toBufferedImage(image);
		
		
		//normal oder gedreht?
		if (this.rotation == 0 || this.rotation == 180) {
			
			double picwidth = image.getWidth(this);
			double picheight = image.getHeight(this);
			double boxwidth = this.getWidth();
			double boxheight = this.getHeight();
			double pic = (double)picwidth / (double)picheight;
			double box = (double)boxwidth / (double)boxheight;
			int width = 0;
			int height = 0;
			int top = 0;
			int left = 0;
		
			if (box>pic) {
				height = (int) boxheight;
				width = (int) (boxheight / picheight * picwidth);
				left = (int) ((boxwidth - width) / 2);
				switch (position[0]) {
				case 0:
					left = 0;
					break;
				case 1:
					left = (int) ((boxwidth - width) / 2);
					break;
				case 2:
					left = (int) boxwidth - width;
				}
			} else {
				width = (int) boxwidth;
				height = (int) (boxwidth / picwidth * picheight);
				switch (position[1]) {
				case 0:
					top = 0;
					break;
				case 1:
					top = (int) ((boxheight - height) / 2);
					break;
				case 2:
					top = (int) boxheight - height;
				}
			}
		
			if ((width < picwidth / 3 * 2 || height < picheight / 3 * 2) && this.blur == 0 || this.blur == 1) {
				output = resizeTrick(output, width, height);
			} else {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
		
			g2.rotate(Math.toRadians(this.rotation), boxwidth / 2, boxheight / 2);
			g2.drawImage(output, left, top, width, height, this);
			
			
			
		} else if (this.rotation == 90 || this.rotation == 270) {
			
			double picheight = image.getWidth(this);
			double picwidth = image.getHeight(this);

			double boxwidth = this.getWidth();
			double boxheight = this.getHeight();
			double pic = (double)picwidth / (double)picheight;
			double box = (double)boxwidth / (double)boxheight;
			int width = 0;
			int height = 0;
			int top = 0;
			int left = 0;
			
			if (box>pic) {
				height = (int) boxheight;
				width = (int) (boxheight / picheight * picwidth);
				left = (int) ((boxwidth - width) / 2);
				switch (position[0]) {
				case 0:
					left = 0;
					break;
				case 1:
					left = (int) ((boxwidth - width) / 2);
					break;
				case 2:
					left = (int) boxwidth - width;
				}
			} else {
				width = (int) boxwidth;
				height = (int) (boxwidth / picwidth * picheight);
				switch (position[1]) {
				case 0:
					top = 0;
					break;
				case 1:
					top = (int) ((boxheight - height) / 2);
					break;
				case 2:
					top = (int) boxheight - height;
				}
			}
			
			//System.out.println("width: " + width + " height: " + height + " top: " + top + " left: " + left + " boxwidth: " + boxwidth + " boxheight: " + boxheight);
			
			if ((width < picwidth / 3 * 2 || height < picheight / 3 * 2) && this.blur == 0 || this.blur == 1) {
				output = resizeTrick(output, width, height);
			} else {
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			}
			
			g2.rotate(Math.toRadians(this.rotation), boxwidth / 2, boxheight / 2);
			g2.drawImage(output, left - (height - width)/2, top  + (height - width)/2, height, width, this);
			//g2.drawRect(left - (height - width)/2, top  + (height - width)/2, height, width);
		}
		this.info.finishProcess();
	}

	public static BufferedImage blurImage(BufferedImage image) {
		float ninth = 1.0f/9.0f;
		float[] blurKernel = {
			ninth, ninth, ninth,
			ninth, ninth, ninth,
			ninth, ninth, ninth
		};
		Map<Key, Object> map = new HashMap<Key, Object>();
		map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		map.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		
		
		RenderingHints hints = new RenderingHints(map);
		BufferedImageOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), ConvolveOp.EDGE_NO_OP, hints);
		return op.filter(image, null);
	}
	private static BufferedImage createCompatibleImage(BufferedImage image) {
		GraphicsConfiguration gc = BufferedImageGraphicsConfig.getConfig(image);
		int w = image.getWidth();
		int h = image.getHeight();
		BufferedImage result = gc.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
		Graphics2D g2 = result.createGraphics();
		g2.drawRenderedImage(image, null);
		g2.dispose();
		return result;
	}
	private static BufferedImage resize(BufferedImage image, int width, int height) {
		int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);

		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
		RenderingHints.VALUE_INTERPOLATION_BILINEAR);

		g.setRenderingHint(RenderingHints.KEY_RENDERING,
		RenderingHints.VALUE_RENDER_QUALITY);

		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
		RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
		}
	private static BufferedImage resizeTrick(BufferedImage image, int width, int height) {
		image = createCompatibleImage(image);
		image = blurImage(image);
		image = resize(image, width, height);
		return image;
	}
	public static BufferedImage toBufferedImage(Image image) {
	    if (image instanceof BufferedImage) {
	        return (BufferedImage)image;
	    }

	    // This code ensures that all the pixels in the image are loaded
	    image = new ImageIcon(image).getImage();

	    // Determine if the image has transparent pixels; for this method's
	    // implementation, see Determining If an Image Has Transparent Pixels
	    boolean hasAlpha = true;

	    // Create a buffered image with a format that's compatible with the screen
	    BufferedImage bimage = null;
	    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	    try {
	        // Determine the type of transparency of the new buffered image
	        int transparency = Transparency.OPAQUE;
	        if (hasAlpha) {
	            transparency = Transparency.BITMASK;
	        }

	        // Create the buffered image
	        GraphicsDevice gs = ge.getDefaultScreenDevice();
	        GraphicsConfiguration gc = gs.getDefaultConfiguration();
	        bimage = gc.createCompatibleImage(
	            image.getWidth(null), image.getHeight(null), transparency);
	    } catch (HeadlessException e) {
	        // The system does not have a screen
	    }

	    if (bimage == null) {
	        // Create a buffered image using the default color model
	        int type = BufferedImage.TYPE_INT_RGB;
	        if (hasAlpha) {
	            type = BufferedImage.TYPE_INT_ARGB;
	        }
	        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
	    }

	    // Copy image to buffered image
	    Graphics g = bimage.createGraphics();

	    // Paint the image onto the buffered image
	    g.drawImage(image, 0, 0, null);
	    g.dispose();

	    return bimage;
	}

	public void redrawlists() {
		this.menuLists.removeAll();
		for (IPicList list: this.listlist) {
			if (list.size() == 0) continue;
			//JMenuItem item = new JMenuItem();
			this.menuLists.add(list.getMenuItem());
			//item.setText(list.getName() + " - " + list.size() + " Pics");
			list.getMenuItem().addActionListener(this);
		}
		
		//Set max width
		int maxwidth = 0;
		for (int i = 0; i < this.menuLists.getItemCount(); i++) {
			JMenuItem item = this.menuLists.getItem(i);
			Dimension d = item.getPreferredSize();
			maxwidth = (d.width > maxwidth) ? d.width : maxwidth;
		}
		for (int i = 0; i < this.menuLists.getItemCount(); i++) {
			JMenuItem item = this.menuLists.getItem(i);
			Dimension d = item.getPreferredSize();
			d.width = maxwidth;
			item.setPreferredSize(d);
		}
		
		//simulate a mouseentered to set the mouseover events
		this.parent.mouseEntered(new MouseEvent(this.menuLists, 0, 0, 0, 0, 0, 0, false));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == this.timer) {
			this.getNext();
			return;
		}
		
		JMenuItem source = (JMenuItem) e.getSource();
		for (IPicList l: listlist) {
			if (l.getMenuItem().equals(source)) {
				setList(l);
				break;
			}
		}

		this.repaint();
		
	}
	
	public void clear() {
		this.mylist = new PictureList("Default");
		this.listlist = new ArrayList<IPicList>();
		this.redrawlists();
		this.repaint();
	}
	
	public void cleanuplists() {
		for (IPicList l: this.listlist) {
			l.cleanup();
		}
		for (int i = 0; i < this.listlist.size(); i++) {
			IPicList list = this.listlist.get(i);
			if (list.size() == 0) this.removelist(list.getName());
		}
	}
	public void removelist(String listname) {
		for (int i = 0; i < this.listlist.size(); i++) {
			String tmpname = this.listlist.get(i).getName();
			if (tmpname.equals(listname)) this.listlist.remove(this.listlist.get(i));
		}
	}
	public void removelistall() {
		this.listlist = new ArrayList<IPicList>();
	}

	@Override
	public void dragEnter(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragExit(DropTargetEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dragOver(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private static final String URI_LIST_MIME_TYPE = "text/uri-list;class=java.lang.String";

	private static ArrayList<File> textURIListToFileList(String data) {
	    ArrayList<File> list2 = new ArrayList<File>(1);
	    for (StringTokenizer st = new StringTokenizer(data, "\r\n"); st.hasMoreTokens();) {
	      String s = st.nextToken();
	      if (s.startsWith("#")) {
	        // the line is a comment (as per the RFC 2483)
	        continue;
	      }
	      try {
	        URI uri = new URI(s);
	        File file = new File(uri);
	        list2.add(file);
	      } catch (URISyntaxException e) {
	        e.printStackTrace();
	      } catch (IllegalArgumentException e) {
	        e.printStackTrace();
	      }
	    }
	    return list2;
	}
	
	@Override
	public void drop(DropTargetDropEvent e) {
		ArrayList<File> result = new ArrayList<File>();
		if (this.isWindows()) {
	      try
	      {
	         Transferable tr = e.getTransferable();

	         if (tr.isDataFlavorSupported (DataFlavor.javaFileListFlavor)) {
	            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
	            @SuppressWarnings("rawtypes")
				java.util.List files = (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
	            
	            // check for database
	            if (files.size() == 1) {
	            	if (files.get(0).toString().endsWith(".db")) setDatabase(files.get(0).toString());
	            }
	            
	            
	            for (Object item: files) result.add(new File(item.toString()));
	            this.autoimportfiles(result);
	            this.repaint();
	            this.redrawlists();
	            e.getDropTargetContext().dropComplete(true);
	         }
	         else
	         {
	        	for (DataFlavor df: tr.getTransferDataFlavors()) {
	        		System.out.println(df.toString());
	        	}
	            System.err.println ("DataFlavor.stringFlavor is not supported, rejected");
	            e.rejectDrop();
	         }
	      }
	      catch (IOException ex)
	      {
	      }
	      catch (UnsupportedFlavorException ex)
	      {
	         System.err.println ("UnsupportedFlavorException");
	         ex.printStackTrace();
	         e.rejectDrop();
	      }
	    //Linux
		} else {
			Transferable transferable = e.getTransferable();
		    DefaultListModel model = new DefaultListModel();

		    e.acceptDrop(DnDConstants.ACTION_MOVE);

		    DataFlavor uriListFlavor = null;
		    try {
		      uriListFlavor = new DataFlavor(URI_LIST_MIME_TYPE);
		    } catch (ClassNotFoundException e2) {
		      e2.printStackTrace();
		    }

		    try {
		      if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
		        ArrayList<?> data = (ArrayList<?>) transferable.getTransferData(DataFlavor.javaFileListFlavor);		        
		     	
		        for (Object o : data) {
		          model.addElement(o);
		        }
		      } else if (transferable.isDataFlavorSupported(uriListFlavor)) {
		        String data = (String) transferable.getTransferData(uriListFlavor);
		        ArrayList<File> files = textURIListToFileList(data);
		        // check for database
	            if (files.size() == 1) {
	            	if (files.get(0).toString().endsWith(".db")) setDatabase(files.get(0).toString());
	            }
		        for (Object item: files) result.add(new File(item.toString()));
		        this.autoimportfiles(result);
		        this.repaint();
	            this.redrawlists();
	            e.getDropTargetContext().dropComplete(true);
		      }
		    } catch (Exception e3) {
		      e3.printStackTrace();
		    }
		}
		this.cleanuplists();
		this.sort();
		this.setList(0);
		this.repaint();
	   }
	
	private void setDatabase(String loc) {
		DBLocation = loc;
		pref.put("db_location", loc);
	}

	private boolean deepfolder = false;
	public void autoimportfiles(ArrayList<File> files) {
		ArrayList<File> topdir = new ArrayList<File>();
		
		for (File file: files) {
			if (file.isFile()) {
				//top dir
				for (String extension: this.picturefilter.getExtensions()) {
					if (file.toString().toLowerCase().endsWith(extension)) {
						topdir.add(file);
					}
				}
			}
			// sub folder
			if (file.isDirectory()) {
				ArrayList<File> dir = new ArrayList<File>();
				dir.addAll(this.getDirectory(file));
				if (!this.deepfolder) {
					
					this.getList(file.getName()).addAll(convertFileArray(dir));
				} else {
					for (File f: dir) {
						try {
							this.getList(new File(f.getParent()).getAbsolutePath()).add(new WebPicture(f.getAbsoluteFile()));
						} catch (MalformedURLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		this.getList(new File(files.get(0).getParent()).getName()).addAll(convertFileArray(topdir));
	}
	public void setDeepfolder(boolean newvalue) {
		this.deepfolder = newvalue;
	}
	public void toggleDeepfolder() {
		this.deepfolder = !this.deepfolder;
	}
	public static Collection<IPicture> convertFileArray(Collection<File> c) {
		ArrayList<IPicture> pic = new ArrayList<IPicture>();
		for (File f: c) {
			try {
				pic.add(new WebPicture(f));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pic;
	}
	public static Collection<IPicture> convertStringArray(String[] c) {
		ArrayList<IPicture> pic = new ArrayList<IPicture>();
		for (String s: c) {
			try {
				pic.add(new WebPicture(s));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return pic;
	}
	
	public ArrayList<File> getDirectory(File dir) {
		System.out.println(dir);
		ArrayList<File> returnfiles = new ArrayList<File>();
		File[] files = dir.listFiles();
		for (File file: files) {
        	if (file.isDirectory()) {
        		returnfiles.addAll(this.getDirectory(file));
        		continue;
        	}
        	for (String extension: this.picturefilter.getExtensions()) {
        		if (file.toString().toLowerCase().endsWith(extension)) {
        			returnfiles.add(file);
        			break;
        		}
        	}
		}
		return returnfiles;
	}
	
	@Override
	public void dropActionChanged(DropTargetDragEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	public boolean isWindows() {
		String osname = System.getProperty("os.name");
		return osname.startsWith("Windows");
	}

	public int rotation = 0;
	public void setRotation(int grad) {
		System.out.println("set rotation to " + grad);
		this.rotation = grad;
		this.repaint();
	}
	
	public void startResize() {
		System.out.println("start resize");
	}
	
	public void stopResize() {
		System.out.println("stop resize");
	}
	
	public void setBlurdefault() {
		this.blur = 0;
	}
	public void setBlurforceenabled() {
		this.blur = 1;
	}
	public void setBlurforcedisabled() {
		this.blur = 2;
	}
	public int setrotateblur() {
		switch (this.blur) {
		case 0: 
			this.blur = 1;
			break;
		case 1:
			this.blur = 2;
			break;
		case 2:
			this.blur = 0;
			break;
		}
		this.repaint();
		return this.blur;
	}
	public int getRotation() {
		return this.rotation;
	}

	public void addList(IPicList newlist) {
		this.listlist.add(newlist);
	}
	
	public void sort() {
		for (IPicList lst: this.listlist) {
			lst.sort();
		}
		ShyviewComparator.ListComparator comp = (new ShyviewComparator()).new ListComparator();
		Collections.sort(this.listlist, comp);
		this.redrawlists();
	}
	
	public void favorite_picture() {
		String listname = this.mylist.getName();
		File acpic = new File(this.mylist.current().getPath());
		File favo_dir = this.get_sub_favorite_folder(listname);
		//try {
			//util.Filesystem.copy(acpic, favo_dir);
		//} catch (IOException e) {e.printStackTrace();}
	}
	public File get_favorite_folder() {
		if (this.favorite_folder == null || !this.favorite_folder.exists()) {
			//new File(util.Filesystem.getUserHomeString(), "Shyview Favorites").mkdir();
		}
		//return new File(util.Filesystem.getUserHomeString(), "Shyview Favorites");
		return new File("/");
	}
	public File get_sub_favorite_folder(String dirname) {
		File tmpf = new File(get_favorite_folder(), dirname);
		if (!tmpf.exists()) tmpf.mkdir();
		return tmpf;
	}
	
	class DBMenuListener implements java.awt.event.MouseListener {
		public void mouseClicked(MouseEvent arg0) {
			if (DBLocation.length() == 0) DBLocation = pref.get("db_location", "");
			if (DBLocation.length() == 0) {
				menuDB.setText("Database unlocated.");
				return;
			}
			menuDB.setText("Connecting to Database..");
			menuDB.removeMouseListener(this);
			try {
				mangaDB = new MangaWatcherDB(DBLocation);
			} catch (Exception e) {
				menuDB.setText("Connection failed.");
				e.printStackTrace();
				return;
			}
			menuDB.setText("Loading Mangas..");
			try {
				mangaDB.loadMangas(menuDB, new DBSelectionListener());
			} catch (SQLException e) {
				menuDB.setText("Loading failed.");
				e.printStackTrace();
				return;
			}
			menuDB.setText("Mangas");
			new MenuScroller(menuDB);
			menuDB.setSelected(false);
		}
		public void mouseEntered(MouseEvent arg0) {	}
		public void mouseExited(MouseEvent arg0) {}
		public void mousePressed(MouseEvent arg0) {}
		public void mouseReleased(MouseEvent arg0) {}
	}
	class DBSelectionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			MangaMenuItem item = (MangaMenuItem) e.getSource();
			clear();
			try {
				listlist = mangaDB.loadChapter(item.getId());
				sort();
			} catch (SQLException e1) {
				e1.printStackTrace();
				menuDB.setText("Cant load Chapters.");
				return;
			}
			System.err.println(item.getId());
		}
	}
	@Override
	public void onWebMateData(String data) {
		clear();
		ArrayList<IPicList> newlists = MangaWatcherDB.loadChapter(data);
		addLists(newlists);
		repaint();
	}
}
