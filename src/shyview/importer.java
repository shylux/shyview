package shyview;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.WindowConstants;


/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class importer extends javax.swing.JFrame {
	private static final long serialVersionUID = 1L;
	private JLabel lblListname;
	private JList listFiles;
	private JScrollPane scrollpane;
	private JButton btnSubmit;
	private JTextField txtListname;
	private String path;
	private DefaultComboBoxModel listFilesModel = new DefaultComboBoxModel();
	public void start() {
		initGUI();
		setLocationRelativeTo(null);
		setVisible(true);
		
	}
	private Picturehandler parent;
	public importer(File f, Picturehandler parent) {
		this.parent = parent;
		if (f.isDirectory()) {
			this.path = f.getPath();
			for (File filename: f.listFiles()) {
				String fn = filename.getName();
				if (fn.endsWith("jpg")) this.listFilesModel.addElement(fn);
				if (fn.endsWith("png")) this.listFilesModel.addElement(fn);
				if (fn.endsWith("gif")) this.listFilesModel.addElement(fn);
			}
		}
	}

	private void initGUI() {
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			thisLayout.rowWeights = new double[] {0.0, 0.0, 0.1};
			thisLayout.rowHeights = new int[] {24, 246, 7};
			thisLayout.columnWeights = new double[] {0.0, 0.0, 0.1, 0.1};
			thisLayout.columnWidths = new int[] {68, 73, 7, 7};
			getContentPane().setLayout(thisLayout);
			this.setTitle("Import Files");
			{
				lblListname = new JLabel();
				getContentPane().add(lblListname, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
				lblListname.setText("Listname:");
			}
			{
				txtListname = new JTextField();
				getContentPane().add(txtListname, new GridBagConstraints(1, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
				txtListname.setText("Default");
			}
			{
				btnSubmit = new JButton();
				getContentPane().add(btnSubmit, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(10, 10, 10, 10), 0, 0));
				getContentPane().add(getScrollpane(), new GridBagConstraints(0, 1, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 5, 0, 5), 0, 0));
				btnSubmit.setText("Import");
				btnSubmit.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						btnSubmitMouseClicked(evt);
					}
				});
			}
			pack();
			this.setSize(228, 377);
		} catch (Exception e) {
		    //add your error handling code here
			e.printStackTrace();
		}
	}
	
	private void btnSubmitMouseClicked(MouseEvent evt) {
		String[] files = new String[listFilesModel.getSize()];
		for (int i = 0; i < listFilesModel.getSize(); i++) {
			files[i] = path + "\\" + (String) listFilesModel.getElementAt(i);
		}
		this.parent.getList(this.txtListname.getText()).addAll(Picturehandler.convertStringArray(files));
		this.parent.redrawlists();
		this.parent.repaint();
		this.setVisible(false);
		this.dispose();
	}
	
	private JScrollPane getScrollpane() {
		if(scrollpane == null) {
			scrollpane = new JScrollPane();
			{
				listFiles = new JList();
				scrollpane.setViewportView(listFiles);
				listFiles.setModel(listFilesModel);
			}
		}
		return scrollpane;
	}

}
