package shyview;

import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MangaWatcherDB {
	private Connection conn;
	
	public MangaWatcherDB(String dblocation) throws SQLException, ClassNotFoundException {
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:"+dblocation);
	}
	
	public void testConnection() throws SQLException {
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT * FROM t_manga;");
		while (rs.next()) {
			System.out.println(rs.getString("title"));
		}
		rs.close();
	}
	
	public void loadMangas(JMenu container, ActionListener actionlistener) throws SQLException {
		Statement stat = conn.createStatement();
		ResultSet rs = stat.executeQuery("SELECT * FROM t_manga;");
		while (rs.next()) {
			MangaMenuItem item = new MangaMenuItem(rs.getString("title"));
			item.setId(rs.getInt("_id"));
			item.addActionListener(actionlistener);
			container.add(item);
		}
		rs.close();
	}
	
	public ArrayList<IPicList> loadChapter(int mangaid) throws SQLException {
		ArrayList<PictureList> chapters = new ArrayList<PictureList>();
		
		PreparedStatement ps = conn.prepareStatement("SELECT * FROM t_chapter WHERE manga_id = ?;");
		ps.setInt(1, mangaid);
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			PictureList chapt = new PictureList(rs.getString("title"));
			String strimages = rs.getString("pages");
			if (strimages.length() == 0) continue;
			String[] arrimages = strimages.split(",");
			for (String imgpair: arrimages) {
				String url = imgpair.split("~")[0];
				try {
					WebPicture pic = new WebPicture(url);
					chapt.add(pic);
				} catch (MalformedURLException e) {
					System.err.println("Malformed! "+url);
				}
			}
			chapters.add(chapt);
		}
		rs.close();
		
		ArrayList<IPicList> out = new ArrayList<IPicList>();
		for (PictureList ch: chapters) {
			out.add(ch);
		}
		return out;
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
							WebPicture pic = new WebPicture(url);
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
	
	class MangaMenuItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		private int id;
		public MangaMenuItem(String title) {
			super(title);
		}
		public void setId(int parid) {
			id = parid;
		}
		public int getId() {
			return id;
		}
	}
}
