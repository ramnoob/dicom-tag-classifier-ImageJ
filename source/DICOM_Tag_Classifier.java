import ij.*;
import ij.plugin.*;
import ij.plugin.frame.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public class DICOM_Tag_Classifier extends PlugInFrame {

	// member variable
	JTextField input_t = new JTextField(16);
	JTextField output_t = new JTextField(16);
	JTextField search_t = new JTextField();
	JTextField min = new JTextField(3);
	JTextField max = new JTextField(3);
	JTextField memo = new JTextField(4);
	JTextField group = new JTextField(3);
	JTextField element = new JTextField(3);
	JTextField value = new JTextField(4);
	List<String> dicomFilePaths = new ArrayList<>();
	List<String> uniqueTags = new ArrayList<>();
	JList tagslist = new JList();
	DefaultListModel<String> taglist_model = new DefaultListModel<>();
	DefaultListModel<String> dir_model = new DefaultListModel<>();
    // Create table model
	DefaultTableModel table_model = new DefaultTableModel() {
	    @Override
	    public boolean isCellEditable(int row, int column) {
	        // Prohibits editing of the second column (index 1)
	        return column != 1;
	    }
	};
    // Set table model to JTable
	JTable dir_tagstable = new JTable(table_model) {
	    @Override
	    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
	        Component component = super.prepareRenderer(renderer, row, column);
	        if (isRowSelected(row)) {
	            component.setBackground(getSelectionBackground());
	            //component.setForeground(getSelectionForeground());
	        } else {
	            if (getRowCount() == 0) {
	                component.setBackground(Color.WHITE);
	            } else {
	                component.setBackground(row % 2 == 0 ? Color.WHITE : getBackground());
	                //component.setForeground(getForeground());
	            }
	        }
	        return component;
	    }
	};
    
	DefaultListModel<String> name_model = new DefaultListModel<>();
	DefaultListModel<String> range_model = new DefaultListModel<>();
	DefaultListModel<String> advanced_model = new DefaultListModel<>();
	JLabel statusLabel = new JLabel("Ready");
	JProgressBar progressBar = new JProgressBar();
	JTabbedPane path_tab = new JTabbedPane();
	JComboBox name_cb = new JComboBox();
	JComboBox range_cb = new JComboBox();
	JComboBox advanced_cb = new JComboBox();
	
	private boolean popup_shown = false;
	private boolean rename = false;
	private boolean cancelRequested = false;
	
	public DICOM_Tag_Classifier() {
		
		super("DICOM Tag Classifier");
		setSize(650, 450);
		EtchedBorder border =new EtchedBorder(EtchedBorder.LOWERED);

		JPanel io_panel = new JPanel();
		io_panel.setPreferredSize(new Dimension(600, 65));
		// input panel
		JPanel input_panel = new JPanel();
		io_panel.add(input_panel);
		// input
		input_panel.add(input_t);
		input_panel.setBorder(new TitledBorder(border, "Input Folder"));
		JButton input_b = new JButton("Browse");
		input_panel.add(input_b);
		input_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_input();
			}
		});
		// output panel
		JPanel output_panel = new JPanel();
		// output
		output_panel.add(output_t);
		output_panel.setBorder(new TitledBorder(border, "Output Folder"));
		io_panel.add(output_panel);
		JButton output_b = new JButton("Browse");
		output_panel.add(output_b);
		output_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				select_output();
			}
		});

		JPanel main_panel = new JPanel();
		// available panel
		JPanel tag_panel = new JPanel();
		tag_panel.setPreferredSize(new Dimension(200, 300));
		tag_panel.setBorder(new TitledBorder(border, "Available Tags"));
		main_panel.add(tag_panel);
		tag_panel.setLayout(new BoxLayout(tag_panel, BoxLayout.Y_AXIS));
		tagslist = new JList(taglist_model);
		// Double-click to add
		tagslist.addMouseListener(new MouseAdapter() {
		    public void mouseClicked(MouseEvent e) {
		        if (e.getClickCount() == 2) {
		            JList list = (JList) e.getSource();
		            int index = list.locationToIndex(e.getPoint());
		            if (index >= 0) {
		                String selectedValue = (String) list.getSelectedValue();
		                if (selectedValue != null) {
		                    addSelectedItemToActiveList(taglist_model, path_tab);
		                }
		            }
		        }
		    }
		});

		JScrollPane scroll_tagslist = new JScrollPane(tagslist);
		scroll_tagslist.setPreferredSize(new Dimension(200, 270));
		tag_panel.add(scroll_tagslist);
		search_t.setPreferredSize(new Dimension(200, 30));
		tag_panel.add(search_t);
		// Add KeyListener to search text field
		search_t.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				searchTagsList(taglist_model, uniqueTags); // Call the search method
			}
		});
		
		
		JPanel rule_panel = new JPanel();
		// building path panel
		JPanel path_panel = new JPanel();
		path_panel.setBorder(new TitledBorder(border, "Path Building"));
		path_panel.setPreferredSize(new Dimension(200, 300));
		main_panel.add(path_panel);
		path_tab.setPreferredSize(new Dimension(190, 270));
		// building path tab
		path_panel.add(path_tab);
		// directory panel
		JPanel dir_panel = new JPanel();
		dir_panel.setLayout(new BoxLayout(dir_panel, BoxLayout.Y_AXIS));
		path_tab.add("Directory", dir_panel);
		path_tab.setForegroundAt(0, Color.BLACK);
        // Add columns (columns)
	    dir_tagstable.setFillsViewportHeight(true); // Makes sure the table fills the viewport
        table_model.addColumn("");
        table_model.addColumn("Tag");
        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) dir_tagstable.getColumnModel();
        
		TableColumn Lcolumn = null;
		TableColumn Tcolumn = null;
		Lcolumn = columnModel.getColumn(0);
		Tcolumn = columnModel.getColumn(1);
		Lcolumn.setPreferredWidth(20);
		Tcolumn.setPreferredWidth(170);
        
        // Add table to scroll pane
        JScrollPane scroll_dir_tagstable = new JScrollPane(dir_tagstable);
		scroll_dir_tagstable.setPreferredSize(new Dimension(190, 208));
		dir_panel.add(scroll_dir_tagstable);
		
		JPanel dir_button_panel = new JPanel();
        dir_button_panel.setLayout(new BoxLayout(dir_button_panel, BoxLayout.X_AXIS));
        Dimension buttonMinSize = new Dimension(95, Integer.MAX_VALUE);
        
        JButton serial_b = new JButton("Layer");
		serial_b.setMaximumSize(buttonMinSize);
		dir_button_panel.add(serial_b);
        serial_b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                serial_number(dir_tagstable); 
            }
        });

        JButton same_b = new JButton("Connect");
		same_b.setMaximumSize(buttonMinSize);
		dir_button_panel.add(same_b);
        same_b.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                same_number(dir_tagstable); 
            }
        });
		dir_panel.add(dir_button_panel);
		
		
		// filename panel
		JPanel name_panel = new JPanel();
		name_panel.setLayout(new BoxLayout(name_panel, BoxLayout.Y_AXIS));
		path_tab.add("FileName", name_panel);
		path_tab.setForegroundAt(1, Color.BLACK);
		JList name_tagslist = new JList(name_model);
		// Add name_tagslist to list
		JScrollPane scroll_name_tagslist = new JScrollPane(name_tagslist);
		scroll_name_tagslist.setPreferredSize(new Dimension(190, 202));
		name_panel.add(scroll_name_tagslist);
		name_cb.addItem("Off");
		name_cb.addItem("SOP Instance UID");
		name_cb.addItem("Connect Tags");
		name_panel.add(name_cb);

		// filter panel
		JPanel filter_panel = new JPanel();
		filter_panel.setBorder(new TitledBorder(border, "Filtering"));
		filter_panel.setPreferredSize(new Dimension(200, 300));
		main_panel.add(filter_panel);
		// building filter tab
		JTabbedPane filter_tab = new JTabbedPane();
		filter_tab.setPreferredSize(new Dimension(190, 270));
		filter_panel.add(filter_tab);
		
		// range panel
		JPanel range_panel = new JPanel();
		range_panel.setLayout(new BoxLayout(range_panel, BoxLayout.Y_AXIS));
		filter_tab.add("   Range   ", range_panel);
		filter_tab.setForegroundAt(0, Color.BLACK);
		
		JPanel rangeset_panel = new JPanel();
		JButton range_b = new JButton("Add");
		range_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRange();
			}
		});
		rangeset_panel.setPreferredSize(new Dimension(190,45));
		rangeset_panel.add(min);
		rangeset_panel.add(max);
		rangeset_panel.add(memo);
		rangeset_panel.add(range_b);
		range_panel.add(rangeset_panel);

		JList range_list = new JList(range_model);
		JScrollPane scroll_range_list = new JScrollPane(range_list);
		range_panel.add(scroll_range_list);
		range_cb.addItem("Off");
		range_cb.addItem("Image Number");
		range_cb.addItem("Slice Location");
		range_cb.setPreferredSize(new Dimension(190, 11));
		range_panel.add(range_cb);
		
		// advanced panel
		JPanel advanced_panel = new JPanel();
		advanced_panel.setLayout(new BoxLayout(advanced_panel, BoxLayout.Y_AXIS));
		filter_tab.add("Advanced", advanced_panel);
		filter_tab.setForegroundAt(1, Color.BLACK);
		JPanel advset_panel = new JPanel();
		JButton adv_b = new JButton("Add");
		adv_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAdvanced();
			}
		});
		advset_panel.setPreferredSize(new Dimension(190, 45));
		advset_panel.add(group);
		advset_panel.add(element);
		advset_panel.add(value);
		advset_panel.add(adv_b);
		advanced_panel.add(advset_panel);
		
		JList advanced_list = new JList(advanced_model);
		JScrollPane scroll_advanced_list = new JScrollPane(advanced_list);
		advanced_panel.add(scroll_advanced_list);
		advanced_cb.addItem("Off");
		advanced_cb.addItem("And");
		advanced_cb.addItem("Or");
		advanced_cb.setPreferredSize(new Dimension(190, 11));
		advanced_panel.add(advanced_cb);
		
		JButton start_b = new JButton("Start");
		start_b.setPreferredSize(new Dimension(80, 30));
		// Processing when the start_b button is pressed
		start_b.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		        // Display a message in the status bar when output_t is unspecified
		        if (output_t.getText().isEmpty()) {
		            setStatusText("Output directory is not specified.");
		            return; // End of process
		        }
		        // From here, call the classifyImages() method and perform other processing
		        classifyImages();
		    }
		});
		
	    // Status Bar Settings
		JPanel buttomPanel = new JPanel();

		statusLabel.setLayout(new FlowLayout(FlowLayout.LEFT));
		statusLabel.setPreferredSize(new Dimension(375, 25));
		statusLabel.setBorder(new TitledBorder(border));

	    // Setting up the progress bar
		buttomPanel.add(statusLabel);
	    buttomPanel.add(progressBar);
		buttomPanel.add(start_b);

		JPanel panelBase = new JPanel();
		panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));
		panelBase.add(io_panel);
		panelBase.add(main_panel);
	    // Added status bar and progress bar to GUI
	    panelBase.add(buttomPanel, BorderLayout.SOUTH);
	    
	 // Key listener to delete rows in backspace for JTable
	    KeyListener tableKeyListener = new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent e) {
	            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
	                JTable table = (JTable) e.getSource();
	                int selectedRow = table.getSelectedRow();
	                if (selectedRow != -1) {
	                    DefaultTableModel model = (DefaultTableModel) table.getModel();
	                    model.removeRow(selectedRow);
	                }
	            }
	        }
	    };

	    // Add the key listener to the JTable
	    dir_tagstable.addKeyListener(tableKeyListener);
	    
	    // Key listener to delete elements in backspace
	    KeyListener listKeyListener = new KeyAdapter() {
	        @Override
	        public void keyPressed(KeyEvent e) {
	            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
	                JList list = (JList) e.getSource();
	                int selectedIndex = list.getSelectedIndex();
	                if (selectedIndex != -1) {
	                    DefaultListModel model = (DefaultListModel) list.getModel();
	                    model.remove(selectedIndex);
	                }
	            }
	        }
	    };

	    // Add a key listener to each JList
	    name_tagslist.addKeyListener(listKeyListener);
	    range_list.addKeyListener(listKeyListener);
	    advanced_list.addKeyListener(listKeyListener);

		add(panelBase);
		setVisible(true);
	}
	
	// Method to update status bar text
	private void setStatusText(String text) {
	    statusLabel.setText(text);
	    statusLabel.paintImmediately(statusLabel.getVisibleRect()); // Instant updates in UI thread
	}

	// Method to update the value of the progress bar
	private void setProgressValue(int value) {
	    progressBar.setValue(value);
	    progressBar.paintImmediately(progressBar.getVisibleRect()); // Instant updates in UI thread
	}
	
	//Method to get all DICOM Tags
	public static String getformatInfo(String filePath) {
        DICOM dicom = new DICOM(); // Create an instance of the DICOM class
        String predcminfo = dicom.getInfo(filePath);
        
        List<String> list = new ArrayList<>();
        String lastDicomTag = null; // Variable that holds the last occurrence of a line in DICOM tag format

        // Add string to list separated by a new line
        String[] lines = predcminfo.split("\n");
        for (String line : lines) {
            // Determine if DICOM tag format
            if (line.matches("^\\w{4},\\w{4}.*")) {
                // For DICOM tag format rows, add to the list as is and update the last DICOM tag
                list.add(line);
                lastDicomTag = line;
            } else {
                // If the line is not in DICOM tag format, append to the end of the value of the previous DICOM tag format line
                if (lastDicomTag != null) {
                    int lastIndex = list.size() - 1;
                    String lastDicomTagValue = list.get(lastIndex);
                    list.set(lastIndex, lastDicomTagValue + " " + line);
                }
            }
        }
        
        // Get elements containing line breaks
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            if (line.contains("\n")) {
                // If it contains line breaks, remove line breaks and update the element
                list.set(i, line.replace("\n", ""));
            }
        }
        
        // Combine duplicate elements into one using LinkedHashSet
        list = new ArrayList<>(new LinkedHashSet<>(list));
        // Convert list contents to string
        String dcmInfo = String.join("\n", list);
        
        return dcmInfo;
	}
	
	// Method to retrieve an arbitrary DICOM tag
	public static String getformatTag(String filePath, String dcm_tag) {
	    String dcmInfo = getformatInfo(filePath); // Obtain DICOM information

	    // Delete lines with ": " followed by a space or no value
	    List<String> list = new ArrayList<>();
	    String[] lines = dcmInfo.split("\n");
	    for (String line : lines) {
	        int colonIndex = line.indexOf(":");
	        if (colonIndex != -1 && colonIndex + 2 == line.length()) {
	            continue;
	        }
	        list.add(line);
	    }

	    // Get DICOM tag value
	    String tagValue = "None";
	    for (String line : list) {
	        if (line.startsWith(dcm_tag)) {
	            int colonIndex = line.indexOf(":");
	            if (colonIndex != -1) {
	                tagValue = line.substring(colonIndex + 1).trim();
	                break;
	            }
	        }
	    }
	    return tagValue;
	}


    public void select_input() {
        dicomFilePaths = new ArrayList<>();
        setStatusText("Finding DICOM files...");
        String inputDir = IJ.getDirectory("Input directory");
        input_t.setText(inputDir);
        setStatusText("Finding DICOM files...Please wait a moment...");
        

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
            	progressBar.setIndeterminate(true);
                ForkJoinPool pool = new ForkJoinPool();
                dicomFilePaths = pool.invoke(new DicomFileFinderTask(new File(inputDir)));
                return null;
            }

            @Override
            protected void done() {
                SwingUtilities.invokeLater(() -> {
                    List<String> shuffledPaths = new ArrayList<>(dicomFilePaths);
                    Collections.shuffle(shuffledPaths);

                    uniqueTags.clear();
                    for (int i = 0; i < Math.min(shuffledPaths.size(), 3); i++) {
                        String filePath = shuffledPaths.get(i);
                        addUniqueTags(filePath, uniqueTags);
                    }

                    taglist_model.clear();
                    for (String tag : uniqueTags) {
                        taglist_model.addElement(tag);
                    }
                    setStatusText("Found " + dicomFilePaths.size() + " DICOM files.");
                    progressBar.setIndeterminate(false);
                });
            }
        };

        worker.execute();
    }
    

	public void select_output() {
		String outputDir = IJ.getDirectory("Output directory");
		output_t.setText(outputDir);
	}
	
    private boolean isDICOMFile(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[132];
            if (fis.read(buffer) == 132) {
                String header = new String(buffer, 128, 4);
                return "DICM".equals(header);
            }
        } catch (IOException e) {
            // error handling
        }
        return false;
    }

    private class DicomFileFinderTask extends RecursiveTask<List<String>> {
        private final File directory;

        DicomFileFinderTask(File directory) {
            this.directory = directory;
        }

        @Override
        protected List<String> compute() {
            List<String> dicomFiles = new ArrayList<>();
            List<DicomFileFinderTask> tasks = new ArrayList<>();

            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        DicomFileFinderTask task = new DicomFileFinderTask(file);
                        task.fork(); // Asynchronous execution of subtasks
                        tasks.add(task);
                    } else if (file.isFile() && isDICOMFile(file)) {
                        ImagePlus imp = IJ.openImage(file.toString());
                        if (imp != null) {
                            dicomFiles.add(file.getAbsolutePath());
                        }
                    }
                }

                for (DicomFileFinderTask task : tasks) {
                    dicomFiles.addAll(task.join()); // Add Dicom file
                }
            }
            return dicomFiles;
        }
    }

	private void addUniqueTags(String filePath, List<String> uniqueTags) {
	    String dcminfo = getformatInfo(filePath); // Obtain DICOM information
	    String[] lines = dcminfo.split("\n"); // Split DICOM information into rows

	    // Get the part from each line up to ":" and add non-duplicate tags to the list
	    for (String line : lines) {
	        int colonIndex = line.indexOf(":");
	        if (colonIndex != -1) {
	            String tag = line.substring(0, colonIndex).trim();
	            if (!uniqueTags.contains(tag)) {
	                uniqueTags.add(tag);
	            }
	        }
	    }
	}
	
	public void searchTagsList(DefaultListModel<String> taglist_model, List<String> uniqueTags) {
		List<String> allTags = new ArrayList<>(uniqueTags); // Make a copy of uniqueTags
		// Format filter text to ignore case differences
		String searach = search_t.getText();
		searach = searach.toLowerCase().replace(" ", "").replace(",", "").replace(":", "");
		// Filter enumerated tags
		taglist_model.clear(); // Clear enumerated tag list
		for (String tag : allTags) {
			// Format the tag string and search ignoring case
			String formattedTag = tag.toLowerCase().replaceAll("[ ,:]", "");
			if (formattedTag.contains(searach)) {
				taglist_model.addElement(tag); // Add tags matching the filter text to the list
			}
		}
	}

	public void addSelectedItemToActiveList(DefaultListModel<String> tagListModel, JTabbedPane tabbedPane) {
	    // Retrieve selected items
	    List<String> selectedItems = tagslist.getSelectedValuesList();
	    // Get index of active tab
	    int selectedIndex = tabbedPane.getSelectedIndex();
	    // Get the JList corresponding to the selected tab
	    // Processing when the tab is the 0th
	    if (selectedIndex == 0) {
	        for (String selectedItem : selectedItems) {
	            // Add rows after checking for duplicates
	            if (!isItemExists(selectedItem)) {
	                int rowCount = dir_tagstable.getRowCount() + 1;
	                table_model.addRow(new Object[]{rowCount, selectedItem});
	            }
	        }
	    } else if(selectedIndex == 1) {
	        for (String selectedItem : selectedItems) {
	            // Add only if there is no selectedItem in name_model
	            if (!name_model.contains(selectedItem)) {
	                name_model.addElement(selectedItem);
	            }
	        }
	    }
	}
	
	// Helper method to check if the specified item exists in table_model
	private boolean isItemExists(String item) {
	    for (int row = 0; row < table_model.getRowCount(); row++) {
	        Object value = table_model.getValueAt(row, 1); // Get the value of the second column
	        if (value != null && value.toString().equals(item)) {
	            return true;
	        }
	    }
	    return false;
	}	
	
	private void serial_number(JTable table) {
		
	    if (dir_tagstable.isEditing()) {
	        dir_tagstable.clearSelection();
	        dir_tagstable.getCellEditor().stopCellEditing();
	    };
	    // Check if the model is an instance of DefaultTableModel to make it editable
	    if (table_model instanceof DefaultTableModel) {
	        DefaultTableModel defaultModel = (DefaultTableModel) table_model;

	        // Iterate through each row of the table
	        for (int rowIndex = 0; rowIndex < defaultModel.getRowCount(); rowIndex++) {
	            // Set the sequential number in the first column (column index 0)
	            defaultModel.setValueAt(rowIndex + 1, rowIndex, 0);
	        }
	    }
	}
	
	private void same_number(JTable table) {

	    if (dir_tagstable.isEditing()) {
	        dir_tagstable.clearSelection();
	        dir_tagstable.getCellEditor().stopCellEditing();
	    };
	    
	    // Check if the model is an instance of DefaultTableModel to make it editable
	    if (table_model instanceof DefaultTableModel) {
	        DefaultTableModel defaultModel = (DefaultTableModel) table_model;

	        // Iterate through each row of the table
	        for (int rowIndex = 0; rowIndex < defaultModel.getRowCount(); rowIndex++) {
	            // Set the value "1" in the first column (column index 0)
	            defaultModel.setValueAt(1, rowIndex, 0);
	        }
	    }
	}
	
	// Add Range Method
	private void addRange() {
	    String minText = min.getText();
	    String maxText = max.getText();
	    String memoText = memo.getText();
	    
	    if (!minText.isEmpty() && !maxText.isEmpty()) {
	        double minValue = Double.parseDouble(minText);
	        double maxValue = Double.parseDouble(maxText);
	        
	        // If a negative value is entered, the relationship between min and max is re-stated correctly.
	        double correctedMinValue = Math.min(minValue, maxValue);
	        double correctedMaxValue = Math.max(minValue, maxValue);
	        
	        String rangeText = String.format("%s ~ %s : %s", correctedMinValue, correctedMaxValue, memoText);
	        range_model.addElement(rangeText);
	    } else {
	        JOptionPane.showMessageDialog(this, "Please enter both minimum and maximum values.", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
	// Add Advanced Method
	private void addAdvanced() {
	    String groupText = group.getText();
	    String elementText = element.getText();
	    String valueText = value.getText();
	    
	    if (!groupText.isEmpty() && !elementText.isEmpty() && !valueText.isEmpty()) {
	        String advancedText = String.format("%s,%s %s", groupText, elementText, valueText);
	        advanced_model.addElement(advancedText);
	    } else {
	        JOptionPane.showMessageDialog(this, "Please enter values for group, element, and value.", "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
	
    // Define the get_tag_values method as you would any other method.
    private List<String> getpathlistItems(DefaultListModel<String> model) {
        List<String> listitems = new ArrayList<>();
        List<String> tagitems = new ArrayList<>();
        listitems = Collections.list(model.elements());
        for (String item : listitems) {
        	String tag_group = item.substring(0, 4); // Get group
        	String tag_element = item.substring(5, 9); // Get element
        	String dicom_tag = String.format("%s,%s", tag_group, tag_element);
        	tagitems.add(dicom_tag);
        }
        return tagitems;
    }

    // Method to add data in the specified column to the list
    public List<Object> getColumnData(TableModel model, int columnIndex) {
        List<Object> columnData = new ArrayList<>();
        int rowCount = model.getRowCount();
        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
            Object cellValue = model.getValueAt(rowIndex, columnIndex);
            if (cellValue == null || cellValue.toString().isEmpty()) {
            	JOptionPane.showMessageDialog(null, "Numbering in Directory tab is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            	cancelRequested = true;
            }
            columnData.add(cellValue);
        }
        return columnData;
    }

    public List<String> convertListToLayer(List<String> data) {
        List<String> result = new ArrayList<>();
        dir_tagstable.clearSelection();
        
        for (int i = 1; i < data.size(); i++) {
            int currentValue = Integer.parseInt(data.get(i));
            int previousValue = Integer.parseInt(data.get(i - 1));

            if (currentValue == previousValue) {
                result.add("_");
            } else if (currentValue > previousValue) {
                result.add("/");
            } else {
            	JOptionPane.showMessageDialog(this, "Numbering in Directory tab is incorrect.", "Error", JOptionPane.ERROR_MESSAGE);
            	cancelRequested = true;
            }
        }
        
        if (data.size()!=0) {
        	result.add("/");
        }
        return result;
    }
    
    // Method to convert a List<Object> to a List<String>.
    public static List<String> convertToStringList(List<Object> data) {
        List<String> stringList = new ArrayList<>();
        for (Object obj : data) {
            stringList.add(obj.toString());
        }
        return stringList;
    }
    

	// Classify Images Method
	private void classifyImages() {
		popup_shown = false;
		rename = false;
		cancelRequested = false;

	    String outputFolderPath = output_t.getText();
	    //List<String> dir_listItems = getpathlistItems(dir_model);
	    
	    if (dir_tagstable.isEditing()) {
	        dir_tagstable.clearSelection();
	        dir_tagstable.getCellEditor().stopCellEditing();
	    };
	    
        // Add column-by-column data to the list
        List<Object> layerData = getColumnData(table_model, 0);
        List<Object> tagData = getColumnData(table_model, 1);
        if (cancelRequested) {
            return; // Exit loop if cancellation is requested
        }
        
        // Converts to string based on rules
        List<String> layerList = convertToStringList(layerData);
        List<String> layerResult = convertListToLayer(layerList);
        if (cancelRequested) {
            return; // Exit loop if cancellation is requested
        }
        
        // Convert List<Object> to List<String>
        List<String> dir_listItems = convertToStringList(tagData);
	    List<String> name_listItems = getpathlistItems(name_model);
	    
	    // New list to store paths for classification
	    List<String> classifyFiles = new ArrayList<>(dicomFilePaths);
	    
	    int totalFiles = classifyFiles.size();
	    
	    if (!range_cb.getSelectedItem().equals("Off")) {
	        Object rangeFilterType = range_cb.getSelectedItem();
	        setStatusText("Applying range filter...");
	        List<String> filteredFiles = new ArrayList<>();
	        for (int i = 0; i < classifyFiles.size(); i++) {
	            String filePath = classifyFiles.get(i);
	            if (isWithinRange(filePath, rangeFilterType)) {
	                filteredFiles.add(filePath);
	            }
	            setStatusText("Applying range filter... " + (i + 1) + "/" + totalFiles);
	        }
	        if(filteredFiles.size()!=0) {
	        	classifyFiles = filteredFiles;
	        	totalFiles = classifyFiles.size();
	        	
	        }else {
	        	IJ.showMessage("Error", "No DICOM files found. Check the filtering conditions.");
	        	return; // Display error message and terminate the process
	        }
	    };
	    
	    if (!advanced_cb.getSelectedItem().equals("Off")) {
	        Object advancedFilterType = advanced_cb.getSelectedItem();
	        setStatusText("Applying advanced filter...");
	        List<String> filteredFiles = new ArrayList<>();
	        for (int i = 0; i < classifyFiles.size(); i++) {
	            String filePath = classifyFiles.get(i);
	            if (isWithinAdvanced(filePath, advancedFilterType)) {
	                filteredFiles.add(filePath);
	            }
	            setStatusText("Applying advanced filter... " + (i + 1) + "/" + totalFiles);
	        }
	        if(filteredFiles.size()!=0) {
	        	classifyFiles = filteredFiles;
	        	totalFiles = classifyFiles.size();
	        }else {
	        	IJ.showMessage("Error", "No DICOM files found. Check the filtering conditions.");
	        	return;
	        	} // Display error message and terminate the process
	    };
	        
	    for (int i = 0; i < totalFiles; i++) {
	        String filePath = classifyFiles.get(i);            
	        processDICOMFile(filePath, outputFolderPath, dir_listItems, layerResult, name_listItems, i);
	        	        
	        // Check if the process should be canceled after each iteration
	        if (cancelRequested) {
	            break; // Exit loop if cancellation is requested
	        }
	        
	        // Update progress bar value and status bar text
	        int progress = (int) ((i + 1) * 100 / totalFiles);
	        setProgressValue(progress);
	        setStatusText("Processed file " + (i + 1) + "/" + totalFiles);
	    }
	    // Displays a message when the process is complete
        if (!cancelRequested) {
    	    setStatusText("Classification complete.");
        }
	}

	private boolean isWithinRange(String filePath, Object filterType) {
	        
	        // Set DICOM tag keys according to filter type
	        String tagKey = null;
	        if (filterType.equals("Image Number")) {
	            tagKey = "0020,0013";
	        } else if (filterType.equals("Slice Location")) {
	            tagKey = "0020,1041";
	        }
	        
	        // Get the value of a DICOM tag
        	double filteredValue = Double.parseDouble(getformatTag(filePath, tagKey));

	        // Checks if there is a DICOM tag value within the specified range
	        for (int i = 0; i < range_model.size(); i++) {
	            String rangeText = range_model.getElementAt(i);
	            double minValue = Double.parseDouble(rangeText.split(" ~ ")[0]);
	            double maxValue = Double.parseDouble(rangeText.split(" ~ ")[1].split(" : ")[0]);
	            if (minValue <= filteredValue && filteredValue <= maxValue) {
	                return true;
	            }
	        }
	    return false;
	}
	
	private boolean isWithinAdvanced(String filePath, Object filterType) {

	    boolean conditionMet = false;

	    for (int i = 0; i < advanced_model.size(); i++) {
	        String advancedText = advanced_model.getElementAt(i);
	        String[] parts = advancedText.split("\\s+");
	        String groupValue = parts[0].split(",")[0];
	        String elementValue = parts[0].split(",")[1];
	        String valueValue = parts[1].toLowerCase().replaceAll(" ", "");
	        String tagValue= String.format("%s,%s", groupValue, elementValue);
	        // Get DICOM tag value
	        String filteredTag = getformatTag(filePath, tagValue).toLowerCase().replaceAll(" ", "").replaceAll("\r\n", "").trim();
	        // Compare tag values with filter values and check for a match
	        if (filterType.equals("And")) {
	            // Check to see if all tags match
	            if (filteredTag.equals(valueValue)) {
	            	conditionMet = true;
	            } else {
	            	conditionMet = false;
	                break;
	            }
	        } else if (filterType.equals("Or")) {
	            // Check if any of the tags match
	            if (filteredTag.equals(valueValue)) {
	            	conditionMet = true;
	                break;
	            } else {
	            	conditionMet = false;
	            }
	        }
	    }
	    return conditionMet;
	}

	// Process DICOM File Method
	private void processDICOMFile(String filePath, String outputFolderPath, List<String> dirTagItems, List<String> layerResult, List<String> nameItems, int i) {
	    String subDir = outputFolderPath;
	    String fileName = new File(filePath).getName();

	    // Define a method to get tag values
	    List<String> dirtagValues = new ArrayList<>();
	    for (String diritem : dirTagItems) {
	        String value = getformatTag(filePath, diritem).toString().replaceAll("[\\/:*?\"<>|]", "_").trim();
	        dirtagValues.add(value);
	    }

	    // Handle directory name setting button cases
	    if (name_cb.getSelectedItem().equals("SOP Instance UID")) {
	        String uid = getformatTag(filePath, "0008,0018").toString();
	        fileName = uid;
	    } else if (name_cb.getSelectedItem().equals("Connect Tags")) {
	        List<String> nametagValues = new ArrayList<>();
	        for (String nameitem : nameItems) {
	            String namevalue = getformatTag(filePath, nameitem).toString().replaceAll("[\\/:*?\"<>|]", "_").replaceAll("\r\n", "").trim();
	            nametagValues.add(namevalue);
	        }
	        fileName = String.join("_", nametagValues);
	    }

        // Process two lists simultaneously using indexes
        for (int d = 0; d < dirtagValues.size(); d++) {
            String dirTag = dirtagValues.get(d);
            String layer = layerResult.get(d);
            subDir = subDir + dirTag + layer;
        }

	    // Check if range filter is enabled
	    if (!range_cb.getSelectedItem().equals("Off")) {
	        Object filterType = range_cb.getSelectedItem();
	        // Set DICOM tag key based on filter type
	        String tagKey = null;
	        if (filterType.equals("Image Number")) {
	            tagKey = "0020,0013";
	        } else if (filterType.equals("Slice Location")) {
	            tagKey = "0020,1041";
	        }

	        // Loop through all selected range items
	        for (int j = 0; j < range_model.size(); j++) {
	            String rangeDir = range_model.getElementAt(j).replaceAll("[\\/:*?\"<>|]", "_"); // Escape special characters
	            double minValue = Double.parseDouble(rangeDir.split(" ~ ")[0]);
	            double maxValue = Double.parseDouble(rangeDir.split(" ~ ")[1].split("_")[0]);
	            double filteredValue = Double.parseDouble(getformatTag(filePath, tagKey));
	            String subDirWithRange = Paths.get(subDir, rangeDir).toString(); // Append range directory to subDir
	            File rangeSubDir = new File(subDirWithRange);
	            if (!rangeSubDir.exists()) {
	                rangeSubDir.mkdirs();
	            }

	            // Check if the file is within the current range
	            if (minValue <= filteredValue && filteredValue <= maxValue) {
	                // Process the file only if it is within the current range
	                // Create new file path with range sub-directory
	                Path newfilePath = Paths.get(subDirWithRange, fileName);
	                Path orifilePath = Paths.get(filePath);

	                // Handle user choice
	                handleUserChoice(orifilePath, newfilePath, i);
	            }
	        }
	    } else {
	        // Create new directory if it doesn't exist
	        File newDir = new File(subDir);
	        if (!newDir.exists()) {
	            newDir.mkdirs();
	        }

	        Path newfilePath = Paths.get(newDir.toString(), fileName);
	        Path orifilePath = Paths.get(filePath);

	        handleUserChoice(orifilePath, newfilePath, i);
	    }
	}
	
	private String getNewFileName(Path filePath) {
	    String fileName = filePath.getFileName().toString();
	    String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
	    String fileExtension = fileName.substring(fileName.lastIndexOf('.') + 1);
	    int counter = 1;

	    // Append "_1", "_2", "_3", etc. to the end of the file name
	    String newFileName = fileNameWithoutExtension + "_" + counter + "." + fileExtension;
	    while (Files.exists(filePath.resolveSibling(newFileName))) {
	        counter++;
	        newFileName = fileNameWithoutExtension + "_" + counter + "." + fileExtension;
	    }
	    return newFileName;
	}

	// Methods to copy files
	private void copyFile(Path orifilePath, Path newfilePath) {
	    try {
	        Files.copy(orifilePath, newfilePath, StandardCopyOption.REPLACE_EXISTING);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

	// Methods to process user selections by displaying pop-up dialogs
	private void handleUserChoice(Path orifilePath, Path newfilePath, int i) {
	    // Check if the file already exists and popup is not shown yet
	    if (Files.exists(newfilePath) && !popup_shown) {
	        popup_shown = true;
	        int choice = JOptionPane.showOptionDialog(null,
	                "A file with the same name already exists. Do you want to overwrite it?",
	                "File Exists",
	                JOptionPane.YES_NO_CANCEL_OPTION,
	                JOptionPane.QUESTION_MESSAGE,
	                null,
	                new String[]{"Overwrite", "Rename", "Cancel"},
	                "Overwrite");

	        // Process according to user's choice
	        if (choice == JOptionPane.YES_OPTION) {
	            rename = false; // Overwrite
	        } else if (choice == JOptionPane.NO_OPTION) {
	            rename = true; // Rename
	        } else {
	            // Cancel
	            cancelRequested = true; // Flag records that a cancellation was requested
		        setStatusText("Canceled...");
		        try {
		            // Wait 2 seconds
		            Thread.sleep(2000);
		        } catch (InterruptedException e) {
		            e.printStackTrace();
		        }
		        setProgressValue(0);
		        setStatusText("Found " + dicomFilePaths.size() + " DICOM files.");
	            return; // If cancel is selected, subsequent processing stops
	        }
	    }

	    // Process according to user's choice
	    try {
	        if (!rename) {
	            // Overwrite
	            Files.copy(orifilePath, newfilePath, StandardCopyOption.REPLACE_EXISTING);
	        } else {
	            // Rename
	            String newFileName = getNewFileName(newfilePath);
	            Files.copy(orifilePath, newfilePath.resolveSibling(newFileName));
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {
		new DICOM_Tag_Classifier();
	}
}
