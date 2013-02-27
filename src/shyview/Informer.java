package shyview;

import java.awt.image.ImageObserver;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class Informer extends JPanel implements ImageObserver{
	private static final long serialVersionUID = 1L;
	private ImageIcon waitingimage;
	private JLabel label;
	
	public Informer() {
		this.setBorder(BorderFactory.createEmptyBorder());
		this.waitingimage = new ImageIcon(getClass().getResource("wait.gif"));
		this.label = new JLabel();
		this.label.setText("test");
		
		this.label.setIcon(this.waitingimage);
		this.add(this.label);
		this.label.setBounds(0, 0, this.getWidth(), this.getHeight());
		//this.setSize(96, 32);
	}
	
	public void setText(String newtext) {
		
	}
	
	/*
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(this.waitingimage, 0, 0, 32, 32, this);
		g.drawChars(text.toCharArray(), 0, text.length(), 32, 20);
	}
	*/
	
	public boolean imageUpdate( ImageObserver imageObserver ) {
		repaint();
		return true;
	}
}
