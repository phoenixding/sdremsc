package edu.cmu.cs.sb.drem;
import edu.cmu.cs.sb.core.*;

import javax.swing.*;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.text.NumberFormat;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.io.*;

/**
 * Class that encapsulates a GO Enrichment table in DREM
 */
public class DREMGui_GOTable extends JPanel implements ActionListener
{
	JFrame theFrame;
	DREM_DataSet theDataSet;
	GoAnnotations.GoResults tgr;
	final static Color bg = Color.white;
	final static Color fg = Color.black;
	JButton saveButton;
	JButton copyButton;

	String[][] tabledata= null;
	String[] columnNames = null;
	double[] dweightA;
	boolean[] include;

	boolean bjustgo;  
	TableSorter sorter;
	HashSet htinames;
	String szTitle;
	DREMGui theDREMGui;

	/**
	 * Constructor builds the GO enrichment table
	 */
	public DREMGui_GOTable(final DREMGui theDREMGui, JFrame theFrame,DREM_DataSet theDataSet, 
			int[][] bindingpval,int[][] bindingpvalindex,
			int[][] bindingpvalTF,int[][] bindingpvalTFindex,
			String[] tfNames, final boolean[] include)
	{
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(bg);
		setForeground(fg);
		this.theFrame = theFrame;
		this.theDataSet = theDataSet;
		this.theDREMGui = theDREMGui;
		this.include = include;

		dweightA = new double[include.length];
		for (int nindex = 0; nindex < dweightA.length; nindex++)
		{
			if (include[nindex])
			{
				dweightA[nindex] = 1;
			}
			else
			{
				dweightA[nindex] = 0;
			} 
		}

		int numcols = 8;
		columnNames = new String[numcols];

		columnNames[0] = "Category ID";
		columnNames[1] = "Category Name";
		columnNames[2] = "#Genes\nCategory";
		columnNames[3] = "#Genes\nAssigned";
		columnNames[4] = "#Genes\nExpected";
		columnNames[5] = "#Genes\nEnriched";
		columnNames[6] = "p-value";
		columnNames[7] = "Corrected\np-value";

		tgr = theDataSet.tga.getCategory(theDataSet.genenames,dweightA);

		if (tgr.goRecArray.length ==0)
		{
			JLabel emptyLabel = new JLabel("  No subset of "+theDataSet.tga.nmingo +
					" or more genes assigned to this set being analyzed"+	 
					" belong to a common GO category at a level "+
					theDataSet.tga.nmingolevel+ 
			" or below in the GO hierarchy.");    
			emptyLabel.setPreferredSize(new Dimension(800,30));
			add(emptyLabel);
		}
		else
		{
			tabledata = new String[tgr.goRecArray.length][columnNames.length];

			NumberFormat nf3 = NumberFormat.getInstance(Locale.ENGLISH);
			nf3.setMinimumFractionDigits(3);
			nf3.setMaximumFractionDigits(3);

			NumberFormat nf1 =  NumberFormat.getInstance(Locale.ENGLISH);
			nf1.setMinimumFractionDigits(1);
			nf1.setMaximumFractionDigits(1);
			nf1.setGroupingUsed(false);

			for (int nindex = 0; nindex < tgr.goRecArray.length; nindex++)
			{
				tabledata[nindex][0] = tgr.goRecArray[nindex].szgoid;
				tabledata[nindex][1] = tgr.goRecArray[nindex].szgocategory;
				tabledata[nindex][6] = Util.doubleToSz(tgr.goRecArray[nindex].dpvalue);
				if (tgr.goRecArray[nindex].dcorrectedpvalue < 0.001)
				{
					if (theDataSet.tga.brandomgoval)
					{
						tabledata[nindex][7] = "<0.001";
					}
					else
					{
						tabledata[nindex][7] = Util.doubleToSz(tgr.goRecArray[nindex].dcorrectedpvalue);
					} 
				}
				else 
				{
					tabledata[nindex][7] = nf3.format(tgr.goRecArray[nindex].dcorrectedpvalue);
				}

				tabledata[nindex][2] = ""+(int) tgr.goRecArray[nindex].dcategoryall;
				tabledata[nindex][3] = nf1.format(tgr.goRecArray[nindex].dcategoryselect);
				double dexpected =  tgr.dselect*tgr.goRecArray[nindex].dcategoryall/tgr.dall;
				tabledata[nindex][4] = nf1.format(dexpected);
				double denriched = tgr.goRecArray[nindex].dcategoryselect - dexpected;
				tabledata[nindex][5] = nf1.format(denriched);
				double dval = Double.parseDouble(tabledata[nindex][5]);
				if (dval > 0)
				{
					tabledata[nindex][5] = "+" + tabledata[nindex][5];
				}
				else if (dval == 0)
				{
					tabledata[nindex][5] = "0.0";
				}
			}

			sorter = new TableSorter(new TableModelST(tabledata, columnNames));
			final JTable table = new JTable(sorter);

			MultiLineHeaderRenderer renderer = new MultiLineHeaderRenderer(sorter);
			Enumeration enumv = table.getColumnModel().getColumns();

			while (enumv.hasMoreElements()) 
			{
				((TableColumn)enumv.nextElement()).setHeaderRenderer(renderer);
			} 

			sorter.setTableHeader(table.getTableHeader());

			table.setPreferredScrollableViewportSize(new Dimension(800,  
					Math.min((table.getRowHeight()+table.getRowMargin())*
							table.getRowCount(),400)));

			TableColumn column;
			column = table.getColumnModel().getColumn(0);
			column.setPreferredWidth(30);
			column = table.getColumnModel().getColumn(1);
			column.setPreferredWidth(200);
			column = table.getColumnModel().getColumn(2);
			column.setPreferredWidth(20);
			column = table.getColumnModel().getColumn(3);
			column.setPreferredWidth(20);
			column = table.getColumnModel().getColumn(4);
			column.setPreferredWidth(20);
			column = table.getColumnModel().getColumn(5);
			column.setPreferredWidth(20);
			column = table.getColumnModel().getColumn(6);
			column.setPreferredWidth(20);
			column = table.getColumnModel().getColumn(7);
			column.setPreferredWidth(20);

			//Create the scroll pane and add the table to it.   
			JScrollPane scrollPane = new JScrollPane(table);
			//Add the scroll pane to this panel.
			add(scrollPane);

			copyButton = new JButton("Copy Table", 
					Util.createImageIcon("Copy16.gif"));
			copyButton.setActionCommand("copy");
			copyButton.setMinimumSize(new Dimension(800,20));
			copyButton.addActionListener(this);

			saveButton = new JButton("Save Table", 
					Util.createImageIcon("Save16.gif"));
			saveButton.setActionCommand("save");
			saveButton.setMinimumSize(new Dimension(800,20));
			saveButton.addActionListener(this);

			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			ListSelectionModel rowSM = table.getSelectionModel();
			final DREM_DataSet finaltheDataSet = theDataSet;
			final String[] ftfNames = tfNames;
			final int[][] fbindingpval = bindingpval;
			final int[][] fbindingpvalIndex = bindingpvalindex;
			final int[][] fbindingpvalTF = bindingpvalTF;
			final int[][] fbindingpvalTFIndex = bindingpvalTFindex;

			final TableSorter finalsorter = sorter;
			rowSM.addListSelectionListener(new ListSelectionListener() 
			{
				public void valueChanged(ListSelectionEvent e) 
				{
					//Ignore extra messages.
					if (e.getValueIsAdjusting()) return;

					final ListSelectionModel lsm = (ListSelectionModel)e.getSource();
					if (lsm.isSelectionEmpty()) 
					{
						return;
					} 
					else 
					{
						javax.swing.SwingUtilities.invokeLater(new Runnable() 
						{
							public void run() 
							{
								int selectedRow = lsm.getMinSelectionIndex();

								//id then title
								String szGoCombined = finalsorter.getValueAt(selectedRow,0)+
								" ("+ finalsorter.getValueAt(selectedRow,1)+")";
								String sznewTitle = "Gene List for "+szGoCombined;

								JFrame frame = new JFrame(sznewTitle);
								frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

								DREMGui_GOGeneTable newContentPane = new
								DREMGui_GOGeneTable(theDREMGui,frame, finaltheDataSet,
										theDREMGui.theTimeiohmm,
										fbindingpval,fbindingpvalIndex,fbindingpvalTF,
										fbindingpvalTFIndex,ftfNames,
										(String) finalsorter.getValueAt(selectedRow,0),include);

								newContentPane.setOpaque(true); //content panes must be opaque
								frame.setContentPane(newContentPane);
								frame.setLocation(40,150);
								//Display the window.
								frame.pack();
								frame.setVisible(true);	      
							}
						});      
					}
				}
			});

			JPanel buttonPanel = new JPanel();
			buttonPanel.setBackground(Color.white);
			buttonPanel.add(copyButton);
			buttonPanel.add(saveButton);

			JButton helpButton = new JButton(Util.createImageIcon("Help16.gif"));
			helpButton.addActionListener(this);
			helpButton.setActionCommand("help");
			buttonPanel.add(helpButton);
			buttonPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,20));
			add(buttonPanel);
		}  
	}

	/**
	 * Writes the contents of the table out to a file
	 */    
	public void printFile(PrintWriter pw)
	{      
		for (int ncol = 0; ncol < columnNames.length; ncol++)
		{
			String szcol = columnNames[ncol];
			for (int nch = 0; nch < szcol.length(); nch++)
			{
				char ch = szcol.charAt(nch);
				if (ch == '\n')
				{
					pw.print(" ");
				}
				else
				{
					pw.print(ch);
				}
			}
			if (ncol < columnNames.length-1)
			{
				pw.print("\t");
			}
		}
		pw.println();

		for (int nrow = 0; nrow <tabledata.length; nrow++)
		{
			for (int ncol = 0; ncol < tabledata[nrow].length-1; ncol++)
			{
				pw.print(tabledata[nrow][ncol]+"\t");
			}
			pw.println(tabledata[nrow][tabledata[nrow].length-1]);
		} 
	}

	/**
	 * Copies the content of the table to the clipboard
	 */
	public void writeToClipboard() 
	{
		StringBuffer sbuf =new StringBuffer();
		for (int ncol = 0; ncol < columnNames.length; ncol++)
		{
			String szcol = columnNames[ncol];
			for (int nch = 0; nch < szcol.length(); nch++)
			{
				char ch = szcol.charAt(nch);
				if (ch == '\n')
				{
					sbuf.append(" ");
				}
				else
				{
					sbuf.append(ch);
				}
			}
			if (ncol < columnNames.length-1)
			{
				sbuf.append("\t");
			}
		}
		sbuf.append("\n");

		for (int nrow = 0; nrow <tabledata.length; nrow++)
		{
			for (int ncol = 0; ncol < tabledata[nrow].length-1; ncol++)
			{
				sbuf.append(tabledata[nrow][ncol]+"\t");
			}
			sbuf.append(tabledata[nrow][tabledata[nrow].length-1]+"\n");
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
	 * Responds to actions to the GO table interface
	 */
	public void actionPerformed(ActionEvent e) 
	{
		String szCommand = e.getActionCommand();

		if (szCommand.equals("copy"))
		{
			writeToClipboard();
		}
		if (szCommand.equals("save"))
		{
			try
			{
				int nreturnVal = DREM_IO.theChooser.showSaveDialog(this);
				if (nreturnVal == JFileChooser.APPROVE_OPTION) 
				{
					File f = DREM_IO.theChooser.getSelectedFile();
					PrintWriter pw = new PrintWriter(new FileOutputStream(f)); 
					printFile(pw);
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
		else if (szCommand.equals("help"))
		{
			String szMessage = "The table contains a Gene Ontology (GO) enrichment analysis "+
			"for the set of currently displayed genes ";

			szMessage += " as compared to what would be expected if the same number of genes as assigned to this "+
			"set were randomly selected "+
			"among the entire set of unique genes on the microarray.\n"; 

			szMessage += "\nThe table has eight columns:" +"\n"+
			"1.  Category ID - the ID for the category\n"+
			"2.  Category Name - the name for the category \n"+
			"3.  #Genes Category - the number of genes on the array annotated as "+
			"belonging to this GO category\n"+
			"4.  #Genes Assigned - the number of genes of this GO category that were assigned "+
			"to this set\n"+
			"5.  #Genes Expected - the number of genes of this GO category expected to be "+ 
			"assigned to this set of genes.  This is computed as "+
			"(#Genes_In_Set)*(#Genes_Category)/(#Total_Unique_Genes_on_Array).\n"+
			"For this set  #Genes_In_Set = "+tgr.dselect+" and "+
			"for the data  #Total_Unique_Genes_on_Array="+((int)tgr.dall)+".\n"+
			"6.  #Genes Enriched - The difference between the #Genes Assigned and the "+
			"#Genes Expected\n"+
			"7.  p-value - the uncorrected probability of seeing greater than "+
			"#Genes_Assigned assigned to this set from this GO category than "+
			"were observed by random chance.  "+
			"This is computed based on the Hypergeometric distribution where the number of items "+
			"being drawn is #Genes_In_Set items with #Genes_Category \"success items\", "+
			"and (#Total_Unique_Genes_on_Array\u2212#Genes_Category) \"failure items.\"\n"+
			"8.  Corrected p-value - the p-value corrected for multiple " +                                                                     
			"GO categories being tested simultaneously.  "+
			"This can be based on either a randomization test or a Bonferroni correction "+
			"depending upon which is selected as 'Multiple hypothesis correction method for actual "+
			"size based enrichment' under 'GO Analysis' on the 'Advanced Options' menu.\n\n"+
			"Note:\n"+
			"+Clicking on a row of the table displays the list of genes in "+
			"the set being analyzed that are by of the GO category of the row clicked.\n"+
			"+The table can be sorted by any of the columns by clicking on the desired "+
			" column's header.\n"+
			"+Using the 'Save Table' button the entire table can be saved.\n"; 

			Util.renderDialog(theFrame,szMessage);
		}
	}
}