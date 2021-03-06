package edu.cmu.cs.sb.drem;
import edu.cmu.cs.sb.core.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.datatransfer.*;

/**
 * Class encapsulating a table showing enrichment of gene targets of 
 * transcription factors out of splits
 */
public class DREMGui_SplitTable extends JPanel implements ActionListener
{

	DREM_Timeiohmm theTimeiohmm;
	DREMGui theDREMGui;
	JFrame theFrame;
	String[] columnNames;
	String[][] tabledata;
	JButton saveButton;
	JButton copyButton;
	JButton goButton;
	TableSorter sorter;
	final static Color bgColor = Color.white;
	final static Color fgColor = Color.black;
	DREMGui_FilterStaticModel hmst;
	int numrows;
	DREM_Timeiohmm.Treenode ptr;
	int npathID;
	NumberFormat nf;
	DecimalFormat df;
	int nchild;

	/**
	 * Constructor for a table giving information on a split when there are two paths out of the split
	 */
	public DREMGui_SplitTable(DREMGui theDREMGui, JFrame theFrame, DREM_Timeiohmm theTimeiohmm,
			DREM_Timeiohmm.Treenode ptr, int npathID)
	{
		this.theFrame = theFrame;
		this.theTimeiohmm = theTimeiohmm;
		this.ptr = ptr;
		this.npathID = npathID;
		this.nchild = -1;
		this.theDREMGui = theDREMGui;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(bgColor);
		setForeground(fgColor);  

		numrows = theTimeiohmm.tfNames.length;
		int nsize = theTimeiohmm.hsUniqueInput.size();

		columnNames = new String[8+2*nsize];
		tabledata = new String[numrows][columnNames.length];
		columnNames[0] = "TF";
		columnNames[1] = "Coeff";

		for (int nel = 0; nel < nsize; nel++)
		{
			columnNames[2+nel] = "Low "+theTimeiohmm.dBindingSigns[nel];
			columnNames[2+nsize+nel] = "High "+theTimeiohmm.dBindingSigns[nel];
		}

		columnNames[columnNames.length-6] = "Avg. Low";
		columnNames[columnNames.length-5] = "Avg. High";
		columnNames[columnNames.length-4] = "Diff";
		columnNames[columnNames.length-3] = "Score";
		columnNames[columnNames.length-2] = "Activity";
		columnNames[columnNames.length-1] = "Max Act.";

		double[] params = null;

		if (!theTimeiohmm.BREGDREM)
		{
			params = ptr.tranC.dcoeff;
		}

		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);

		for (int nrow = 0; nrow < tabledata.length; nrow++)
		{
			tabledata[nrow][0] = theTimeiohmm.tfNames[nrow];
			tabledata[nrow][columnNames.length-3] =doubleToSz(ptr.dpvals[nrow][0]);

			if (ptr.nextptr[0].dmean < ptr.nextptr[1].dmean)
			{
				tabledata[nrow][columnNames.length-4] = nf.format(ptr.ddiff[nrow][0]);

				if (params != null)
				{
					tabledata[nrow][1] = nf.format(-params[nrow+1]);
				}
				else
				{
					tabledata[nrow][1] = "0";
				}

				tabledata[nrow][columnNames.length-5] = nf.format(ptr.dscore[nrow][1]);
				tabledata[nrow][columnNames.length-6] = nf.format(ptr.dscore[nrow][0]);

				for (int nel = 0; nel < nsize; nel++)
				{	      
					tabledata[nrow][2+nel] = ""+(int) ptr.ncountvals[nrow][0][nel];
					tabledata[nrow][2+nsize+nel] = "" + (int) ptr.ncountvals[nrow][1][nel];
				}
			}
			else
			{
				tabledata[nrow][columnNames.length-4] = nf.format(-ptr.ddiff[nrow][0]);
				//reverse if not swap because of paramz. of logit

				if (params != null)
				{
					tabledata[nrow][1] = nf.format(params[nrow+1]);
				}
				else
				{
					tabledata[nrow][1] = "0";
				}

				tabledata[nrow][columnNames.length-5] = nf.format(ptr.dscore[nrow][0]);
				tabledata[nrow][columnNames.length-6] = nf.format(ptr.dscore[nrow][1]); 

				for (int nel = 0; nel < nsize; nel++)
				{	      
					tabledata[nrow][2+nel] =  ""+(int) ptr.ncountvals[nrow][1][nel];
					tabledata[nrow][2+nsize+nel] =  ""+(int) ptr.ncountvals[nrow][0][nel];
				}
			}
			
			tabledata[nrow][columnNames.length-2] = lrgNumFmt(ptr.dTFActivityScore[nrow],nf);
			tabledata[nrow][columnNames.length-1] = lrgNumFmt(theTimeiohmm.dMaxTFActivityScore[nrow],nf);
		}

		sorter = new TableSorter(new TableModelST(tabledata,columnNames));
		sorter.setSortingStatus(columnNames.length-3,TableSorter.ASCENDING);
		final JTable table = new JTable(sorter);

		TableColumn column;
		for (int nindex =0; nindex < columnNames.length; nindex++)
		{
			column = table.getColumnModel().getColumn(nindex);
			column.setPreferredWidth(100);
		}

		sorter.setTableHeader(table.getTableHeader());


		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(700, 
				Math.min((table.getRowHeight()+table.getRowMargin())*
						table.getRowCount(),400)));
		add(scrollPane);

		addBottom();
	}

	/**
	 * Constructor for a table giving information on a split when there are three
	 * or more paths out of the split
	 */
	public DREMGui_SplitTable(DREMGui theDREMGui, JFrame theFrame, DREM_Timeiohmm theTimeiohmm,
			DREM_Timeiohmm.Treenode ptr, int npathID, int ntable, int nchild)
	{
		//for 3 or more children

		this.theDREMGui = theDREMGui;
		this.theFrame = theFrame;
		this.theTimeiohmm = theTimeiohmm;
		this.ptr = ptr;
		this.npathID = npathID;
		this.nchild = nchild;

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(bgColor);
		setForeground(fgColor);  

		numrows = theTimeiohmm.tfNames.length;
		int nsize = theTimeiohmm.hsUniqueInput.size();

		columnNames = new String[8+2*nsize];
		tabledata = new String[numrows][columnNames.length];
		columnNames[0] = "TF";
		columnNames[1] = "Coeff";

		String szLabel;
		if (ptr.numchildren == 3)
		{
			if (ntable == 0)
			{
				szLabel = "Low ";
			}
			else if (ntable == 1)
			{
				szLabel = "Mid ";
			}
			else //(nchild == 2)
			{
				szLabel = "High ";
			}
		}
		else
		{
			szLabel = "This ";
		}

		for (int nel = 0; nel < nsize; nel++)
		{
			columnNames[2+nel] = szLabel+theTimeiohmm.dBindingSigns[nel];
			columnNames[2+nsize+nel] = "Other "+theTimeiohmm.dBindingSigns[nel];
		}

		columnNames[columnNames.length-6] = "Avg. "+szLabel.trim();
		columnNames[columnNames.length-5] = "Avg. Other";
		columnNames[columnNames.length-4] = "Diff";
		columnNames[columnNames.length-3] = "Score";
		columnNames[columnNames.length-2] = "Activity";
		columnNames[columnNames.length-1] = "Max Act.";

		double[] params=null;

		if (!theTimeiohmm.BREGDREM)
		{
			params  = ptr.tranC.dcoeff;
		}

		nf = NumberFormat.getInstance(Locale.ENGLISH);
		nf.setMinimumFractionDigits(3);
		nf.setMaximumFractionDigits(3);

		for (int nrow = 0; nrow < tabledata.length; nrow++)
		{
			tabledata[nrow][0] = theTimeiohmm.tfNames[nrow];
			tabledata[nrow][columnNames.length-4] = nf.format(ptr.ddiff[nrow][nchild]);
			tabledata[nrow][columnNames.length-3] =doubleToSz(ptr.dpvals[nrow][nchild]);

			if ((nchild == ptr.numchildren-1)||(params==null))
			{
				tabledata[nrow][1] = "0";
			}
			else
			{
				tabledata[nrow][1] = nf.format(params[(tabledata.length+1)*nchild+nrow+1]);
			}

			tabledata[nrow][columnNames.length-5] = nf.format(ptr.dscoreother[nrow][nchild]);
			tabledata[nrow][columnNames.length-6] = nf.format(ptr.dscore[nrow][nchild]);

			for (int nel = 0; nel < nsize; nel++)
			{	      
				tabledata[nrow][2+nel] = ""+ ptr.ncountvals[nrow][nchild][nel];
				tabledata[nrow][2+nsize+nel] = "" + ptr.nothersum[nrow][nchild][nel];
			}
			
			tabledata[nrow][columnNames.length-2] = lrgNumFmt(ptr.dTFActivityScore[nrow],nf);
			tabledata[nrow][columnNames.length-1] = lrgNumFmt(theTimeiohmm.dMaxTFActivityScore[nrow],nf);
		}

		sorter = new TableSorter(new TableModelST(tabledata,columnNames));
		sorter.setSortingStatus(columnNames.length-3,TableSorter.ASCENDING);
		final JTable table = new JTable(sorter);

		TableColumn column;

		for (int nindex =0; nindex < columnNames.length; nindex++)
		{
			column = table.getColumnModel().getColumn(nindex);
			column.setPreferredWidth(100);
		}

		sorter.setTableHeader(table.getTableHeader());


		JScrollPane scrollPane = new JScrollPane(table);
		table.setPreferredScrollableViewportSize(new Dimension(700, 
				Math.min((table.getRowHeight()+table.getRowMargin())*
						table.getRowCount(),400)));
		add(scrollPane);
		addBottom();
	}


	/**
	 *Outputs the content of the PrintWriter
	 */
	public void printFile(PrintWriter pw)
	{

		for (int ncol = 0; ncol < columnNames.length-1; ncol++)
		{
			pw.print(columnNames[ncol] +"\t");
		}
		pw.println(columnNames[columnNames.length - 1]);

		for (int nrow = 0; nrow <tabledata.length; nrow++)
		{
			for (int ncol = 0; ncol < tabledata[nrow].length-1; ncol++)
			{
				pw.print(sorter.getValueAt(nrow,ncol) +"\t");
			}
			pw.println(sorter.getValueAt(nrow,columnNames.length-1));
		}
	}

	/**
	 * Copies the contents of the table to the clipboard
	 */
	public void writeToClipboard() 
	{
		StringBuffer sbuf =new StringBuffer();
		for (int ncol = 0; ncol < columnNames.length-1; ncol++)
		{
			sbuf.append(columnNames[ncol] +"\t");
		}
		sbuf.append(columnNames[columnNames.length - 1]+"\n");

		for (int nrow = 0; nrow <tabledata.length; nrow++)
		{
			for (int ncol = 0; ncol < tabledata[nrow].length-1; ncol++)
			{
				sbuf.append(sorter.getValueAt(nrow,ncol) +"\t");
			}
			sbuf.append(sorter.getValueAt(nrow,columnNames.length-1)+"\n");
		}
		// get the system clipboard
		Clipboard systemClipboard =
			Toolkit.getDefaultToolkit().getSystemClipboard();
		// set the textual content on the clipboard to our 
		// Transferable object 
		Transferable transferableText = new StringSelection(sbuf.toString());
		systemClipboard.setContents(transferableText, null);
	}

	/**
	 * Responds to actions on the interface
	 */
	public void actionPerformed(ActionEvent e) 
	{
		String szCommand = e.getActionCommand();

		if (szCommand.equals("copy"))
		{
			writeToClipboard();
		}
		else if (szCommand.equals("save"))
		{
			try
			{
				int nreturnVal = DREM_IO.theChooser.showSaveDialog(this);
				if (nreturnVal == JFileChooser.APPROVE_OPTION) 
				{
					File f = DREM_IO.theChooser.getSelectedFile();
					PrintWriter pw = new PrintWriter(new FileOutputStream(f));
					if (szCommand.equals("save"))
					{
						printFile(pw);
					}
					pw.close();
				}
			}
			catch (final FileNotFoundException fex)
			{
				javax.swing.SwingUtilities.invokeLater(new Runnable() 
				{
					public void run() 
					{
						JOptionPane.showMessageDialog(null, fex.getMessage(), 
								"Exception thrown", JOptionPane.ERROR_MESSAGE);
					}
				});
				fex.printStackTrace(System.out);
			}
		}
		else if (szCommand.equals("go"))
		{

			javax.swing.SwingUtilities.invokeLater(new Runnable() 
			{

				public void run() 
				{
					JFrame frame = new JFrame("GO Split Table");
					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.setLocation(150,200);

					JTabbedPane tabbedPane = new JTabbedPane();

					for (int ntable = 0; ntable < ptr.numchildren; ntable++)
					{
						DREMGui_GOSplitTable newContentPane = new DREMGui_GOSplitTable(theDREMGui,frame, theTimeiohmm,
								ptr,ptr.orderA[ntable]);

						String szLabel;
						String szToolTip;

						if (ptr.numchildren <= 3)
						{
							if (ntable == 0)
							{
								szLabel = "Low Path";
								szToolTip = "Low";
							}
							else if ((ntable == 1)&&(ptr.numchildren == 3))
							{
								szLabel = "Middle Path";
								szToolTip = "Middle";
							}
							else 
							{
								szLabel = "High Path";
								szToolTip = "High";
							}
						}
						else
						{
							szLabel = "Child "+ntable+" vs. Others ";
							szToolTip = "Child "+ ntable;
						}
						tabbedPane.addTab(szLabel,null,newContentPane,szToolTip);
					}
					tabbedPane.setOpaque(true);
					frame.setContentPane(tabbedPane);	     

					//Display the window.
					frame.pack();
					frame.setVisible(true);
				}
			});
		}
		else if (szCommand.equals("help"))
		{
			String szMessage = "This table gives information about the selected split."+
			"  Consult section 4.14 of the user manual for more details on this table. ";

			Util.renderDialog(theFrame,szMessage);
		}
	}

	/**
	 * Adds the information that appears at the bottom of the window below the main table
	 */
	private void addBottom()
	{
		JPanel countPanel = new JPanel();
		String szcountLabel = "Total number of genes most likely going through this state is "+ptr.numPath;
		JLabel countLabel = new JLabel(szcountLabel);
		countPanel.setBackground(Color.white);
		countPanel.add(countLabel);
		countPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		add(countPanel);

		JPanel labelPanel =new JPanel();
		String szInfo;
		if (nchild == -1)
		{

			if (ptr.nextptr[0].dmean < ptr.nextptr[1].dmean)
			{
				if (theTimeiohmm.BREGDREM)
				{
					szInfo = "Transition probability of high path is "+nf.format(1-ptr.ptrans[0]);
				}
				else
				{
					szInfo = "Intercept coefficient is " + nf.format(-ptr.tranC.dcoeff[0]);
				}
			}
			else
			{
				if (theTimeiohmm.BREGDREM)
				{
					szInfo = "Transition probability of high path is "+nf.format(ptr.ptrans[0]);
				}
				else
				{
					szInfo = "Intercept coefficient is "+ nf.format(ptr.tranC.dcoeff[0]);
				}
			}
		}
		else
		{
			if (theTimeiohmm.BREGDREM)
			{
				szInfo = "Transition probability is "+nf.format(ptr.ptrans[nchild]);
			}
			else if (nchild == ptr.numchildren-1)
			{
				szInfo = "Intercept coefficient is 0";
			}
			else
			{
				szInfo = "Intercept coefficient is "+nf.format(ptr.tranC.dcoeff[(tabledata.length+1)*nchild]);
			}
		}
		szInfo += "; This "+theTimeiohmm.theDataSet.dsamplemins[ptr.ndepth]+
		" state output distribution is Normal(mu ="+
		nf.format(ptr.dmean)+",sigma = " +nf.format(ptr.dsigma)+")";
		JLabel infoLabel = new JLabel(szInfo);
		labelPanel.setBackground(Color.white);
		labelPanel.add(infoLabel);
		labelPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		add(labelPanel);


		copyButton = new JButton("Copy Table", 
				Util.createImageIcon("Copy16.gif"));
		copyButton.setActionCommand("copy");
		copyButton.setMinimumSize(new Dimension(800,20));
		copyButton.addActionListener(this);

		saveButton = new JButton("Save Table",
				Util.createImageIcon("Save16.gif"));
		saveButton.setActionCommand("save");
		saveButton.setMinimumSize(new Dimension(Integer.MAX_VALUE,20));
		saveButton.addActionListener(this);

		JPanel buttonPanel =new JPanel();
		buttonPanel.setBackground(Color.white);

		goButton = new JButton("GO Split Table");
		goButton.setActionCommand("go");
		goButton.setMinimumSize(new Dimension(Integer.MAX_VALUE,20));
		goButton.addActionListener(this);
		buttonPanel.add(goButton);
		buttonPanel.add(copyButton);
		buttonPanel.add(saveButton);

		JButton helpButton = new JButton(Util.createImageIcon("Help16.gif"));
		helpButton.addActionListener(this);
		helpButton.setActionCommand("help");
		buttonPanel.add(helpButton);
		buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
		add(buttonPanel);
	}

	/**
	 * Converts double values to String values formatted as used in the table
	 */
	public static String doubleToSz(double dval)
	{
		String szexp;
		double dtempval = dval;
		int nexp = 0;

		NumberFormat nf2 = NumberFormat.getInstance(Locale.ENGLISH);
		nf2.setMinimumFractionDigits(3);
		nf2.setMaximumFractionDigits(3);     


		NumberFormat nf1 = NumberFormat.getInstance(Locale.ENGLISH);
		nf1.setMinimumFractionDigits(2);
		nf1.setMaximumFractionDigits(2);  

		if (dval <= 0)
		{
			szexp = "0.000";
		}
		else
		{
			while ((dtempval<0.9995)&&(dtempval>0))
			{
				nexp--;
				dtempval = dtempval*10;
			}

			if (nexp < -2)
			{
				dtempval = Math.pow(10,Math.log(dval)/Math.log(10)-nexp);
				szexp = nf1.format(dtempval)+"e"+nexp;
			}
			else
			{
				szexp = nf2.format(dval);
			}
		}

		return szexp;
	}
	
	/**
	 * Formats a number for the table that may potentially have very large
	 * values.  If the number is less than 1 it is handled by doubleToSz.  If it is
	 * >= 1 and < 1000, format using the NumberFormat's
	 * format.  Otherwise, use scientific notation as specified by the global
	 * DecimalFormat.
	 * @param dNum
	 * @param nf
	 * @return
	 */
	public String lrgNumFmt(double dNum, NumberFormat nf)
	{
		// Format using doubleToSz for small numbers.
		if(dNum < 1)
		{
			return doubleToSz(dNum);
		}
		// Use the NumberFormat for numbers >= 1 and < 1000
		else if(dNum < 1000)
		{
			return nf.format(dNum);
		}
		else
		{
			if(df == null)
			{
				df = new DecimalFormat("0.00E0");
			}
			
			// For consistency with the NumberFormat's handling of small numbers
			return df.format(dNum).replace('E', 'e');
		}
	}

}