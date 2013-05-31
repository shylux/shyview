package shyview;

import java.awt.AlphaComposite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
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
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.ImageObserver;
import java.awt.image.Kernel;
import java.awt.image.MemoryImageSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import shyview.Picture.StillLoadingException;

import webmate.IWebMateListener;
@SuppressWarnings("serial")
public class Picturehandler extends JPanel implements ImageObserver, ActionListener, DropTargetListener, IWebMateListener, MouseMotionListener {
	private ArrayList<IPicList>  listlist = new ArrayList<IPicList>();
	private int list_index = 0;
	private ShyluxFileFilter picturefilter = new ShyluxFileFilter();
	private ShyluxFileFilter jsonfilter = new ShyluxFileFilter();
	private IPicList mylist = new PictureList("Default");
	private JMenu menuLists;
	private Timer timer = new Timer(1000, this);
	private Image defaultimage = new ImageIcon(getClass().getResource("DefaultImage.gif")).getImage();
	private Image errorimage = new ImageIcon(getClass().getResource("ErrorImage.jpg")).getImage();
	private PicViewGUI parent;
	private File favorite_folder = null;
	private Preferences pref;
	private Timer mouseHideTimer = new Timer(4000, this);
	
	Picturehandler(JMenu listcontainer, PicViewGUI parent) {
		TitleInformer.getInstance().setFrame(parent);
		this.parent = parent;
		this.menuLists = listcontainer;
		listlist.add(mylist);
		this.picturefilter.addExtension(".jpg");
		this.picturefilter.addExtension(".png");
		this.picturefilter.addExtension(".gif");
		this.picturefilter.addExtension(".jpeg");
		this.picturefilter.addExtension(".bmp");
		this.picturefilter.addExtension(".img");
	    this.jsonfilter.addExtension(".json");
	    
	    // display default pic
		repaint();
		this.redrawlists();
		
		// dropping images/folders
		new DropTarget(this, this);
		
		// preferences
		pref = Preferences.userNodeForPackage(getClass());

		// default delay
		int timer_delay = pref.getInt("timer_delay", 30);
		if (timer_delay > 0) this.setTimerdelay(timer_delay);
		
		// mouse hide timer
		this.addMouseMotionListener(this); 
		this.mouseHideTimer.start();
	}
	
	public IPicture acpic() {
		try {
			return mylist.current();
		} catch (NoSuchElementException e) {
			if (count() == 0) return null;
			this.getNextList();
			return acpic();
		}
	}
	public IPicList aclist() {
		return mylist;
	}
	
	public void addPicture(String Path, String list) {
		try {
			this.getList(list).add(new Picture(Path));
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
			try {
				mylist.next();
			} catch (NoSuchElementException e) {
				getNextList();
			}
		}
		repaint();
	}
	public void getPrevious() {
		if (mylist != null) {
			if (acpic() != null) acpic().flush();
			try {
				mylist.previous();
			} catch (NoSuchElementException e) {
				getPreviousList();
			}
		}
		repaint();
	}
	public IPicList getNextList() {
		if (this.listlist.size() == 0) return new PictureList("Default");
		list_index += 1;
		if (listlist.size() - 1 < list_index) list_index = 0;
		this.mylist = listlist.get(list_index);
		this.mylist.setIndex(0);
		repaint();
		return this.mylist;
	}
	public IPicList getPreviousList() {
		if (this.listlist.size() == 0) return new PictureList("Default");
		list_index -= 1;
		if (list_index < 0) list_index = listlist.size() - 1;
		this.mylist = listlist.get(list_index);
		this.mylist.setIndex(0);
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
				this.list_index = i;
				mylist.setIndex(0);
			}
		}
	}
	public void setList(IPicList list) {
		list_index = this.listlist.indexOf(list);
		this.mylist = list;
		mylist.setIndex(0);
	}
	public void setList(int index) {
		if (index > this.listlist.size()) index = 0;
		this.list_index = index;
		this.mylist = this.listlist.get(index);
		this.mylist.setIndex(0);
	}
	
	/**
	 * Setting index of actual list. First element is 0.
	 * @param i index. first element is 0
	 */
	public void setPictureIndex(int i) {
		aclist().setIndex(i);
		repaint();
	}
	
	public synchronized void addLists(List<IPicList> lists) {
		listlist.addAll(lists);
		redrawlists();
	}
	
	public boolean imageUpdate( ImageObserver imageObserver ) {
		repaint();
		return true;
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
	public void toggleTimer() {
		if (this.timer.isRunning()) this.timer.stop();
		else this.timer.start();
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
	
	public void paint(Graphics g) {
		super.paint(g);
		
		TitleInformer.getInstance().update(aclist());
	
		int position[] = new int[2];
		position[0] = 1;
		position[1] = 1;
		Image image = this.defaultimage;

		if (count() != 0) {
			//Load image
			TitleInformer.getInstance().pushProcess("Loading picture");
			try {
				
				image = acpic().getPicture();
			} catch (StillLoadingException e) {
				System.out.println(System.currentTimeMillis()+" "+ acpic().getName() +" still loading..");
				repaint(); // recursive. stack overflow possible.
			} catch (FileNotFoundException e1) {
				System.out.println("file not found");
				e1.printStackTrace();
				mylist.remove(acpic());
				TitleInformer.getInstance().clear();
				if (this.errorimage == null) return;
				image = this.errorimage;
			}
		}
		/*
		try {
			while ((image = acpic().getPicture()) == null) {
				System.err.println("Removed "+acpic().getName());
				
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
		*/
		//this.info.setVisible(false);
		
		TitleInformer.getInstance().pushProcess("Processing");
		
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
		
			if (width < picwidth / 3 * 2 || height < picheight / 3 * 2) {
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
			
			if (width < picwidth / 3 * 2 || height < picheight / 3 * 2) {
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
		TitleInformer.getInstance().finishProcess();
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
		} else if (e.getSource() == this.mouseHideTimer) {
			hideMouse();
		} else {
			JMenuItem source = (JMenuItem) e.getSource();
			for (IPicList l: listlist) {
				if (l.getMenuItem().equals(source)) {
					setList(l);
					break;
				}
			}
			this.repaint();
		}

		
		
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
							this.getList(new File(f.getParent()).getAbsolutePath()).add(new Picture(f.getAbsoluteFile()));
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
				pic.add(new Picture(f));
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
				pic.add(new Picture(s));
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
	/*
	public void favorite_picture() {
		String listname = this.mylist.getName();
		File acpic = new File(this.mylist.current().getPath());
		File favo_dir = this.get_sub_favorite_folder(listname);
		//try {
			//util.Filesystem.copy(acpic, favo_dir);
		//} catch (IOException e) {e.printStackTrace();}
	}
	*/
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
	
	@Override
	public void onWebMateData(String data) {
		clear();
		ArrayList<IPicList> newlists = Picturehandler.loadChapter(data);
		addLists(newlists);
		sort();
		list_index = 0;
		repaint();
	}
	
	public static ArrayList<IPicList> loadChapter(String xmldata) {
		ArrayList<PictureList> chapters = new ArrayList<PictureList>();
		
		StringReader inStream = new StringReader(xmldata);
		InputSource inSource = new InputSource(inStream);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(inSource);
			Element rootNode = doc.getDocumentElement();
			NodeList chapterList = rootNode.getElementsByTagName("Chapter");
			for (int i = 0; i < chapterList.getLength(); i++) {
				Node nNode = chapterList.item(i);
				if (nNode.getNodeType() == Node.ELEMENT_NODE) {
					Element chapter = (Element) nNode;
					NodeList title = chapter.getElementsByTagName("Title");
					String chapName = title.item(0).getTextContent();
					NodeList pages = chapter.getElementsByTagName("Pages");
					String chapPages = pages.item(0).getTextContent();
					
					// build list
					PictureList chapt = new PictureList(chapName);
					String[] arrimages = chapPages.split(",");
					int counter = 1;
					for (String imgpair: arrimages) {
						String url = imgpair.split("~")[0];
						try {
							Picture pic = new Picture(url);
							pic.setName(""+counter);
							counter++;
							chapt.add(pic);
						} catch (MalformedURLException e) {
							System.err.println("Malformed! "+url);
						}
					}
					chapters.add(chapt);
				}
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<IPicList> out = new ArrayList<IPicList>();
		for (PictureList ch: chapters) {
			out.add(ch);
		}
		return out;
	}

	
	/* Used to hide Cursor */
	@Override
	public void mouseDragged(MouseEvent arg0) {}
	@Override // show cursor
	public void mouseMoved(MouseEvent arg0) {
		this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	}
	// hide cursor
	private void hideMouse() {
		int[] pixels = new int[16 * 16];
		Image image = Toolkit.getDefaultToolkit().createImage(new MemoryImageSource(16, 16, pixels, 0, 16));
		Cursor transparentCursor = Toolkit.getDefaultToolkit().createCustomCursor(image, new Point(0, 0), "invisibleCursor");
		this.setCursor(transparentCursor);
	}
}
