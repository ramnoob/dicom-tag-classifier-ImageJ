import ij.*;
import ij.io.*;
import ij.io.FileSaver;
import ij.process.*;
import ij.gui.*;
import ij.util.*;
import ij.plugin.*;
import ij.plugin.frame.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

public class DICOM_Classifire extends PlugInFrame {

	//ImageJ ij = new ImageJ(2);
	ImagePlus imp;

	// member variable
	JTextField input_t = new JTextField(15);
	JTextField output_t = new JTextField(15);
	JTextField search_t = new JTextField();
	JTextField min = new JTextField(4);
	JTextField max = new JTextField(4);
	JTextField memo = new JTextField(4);
	JTextField group = new JTextField(4);
	JTextField element = new JTextField(4);
	JTextField value = new JTextField(4);
	// 他のメンバー変数と同じレベルでuniqueTagsを宣言
	List<String> dicomFilePaths = new ArrayList<>();
	List<String> uniqueTags = new ArrayList<>();
	List<JList> setall_tagsLists = new ArrayList<>();
	JList tagslist = new JList();
	DefaultListModel<String> taglist_model = new DefaultListModel<>();
	DefaultListModel<String> dir_model = new DefaultListModel<>();
	DefaultListModel<String> name_model = new DefaultListModel<>();
	DefaultListModel<String> range_model = new DefaultListModel<>();
	DefaultListModel<String> advanced_model = new DefaultListModel<>();
	
	JPanel statusPanel = new JPanel();
	JLabel statusLabel = new JLabel("Ready");
	JProgressBar progressBar = new JProgressBar();
	
	JTabbedPane path_tab = new JTabbedPane();
	
	JComboBox dir_cb = new JComboBox();
	JComboBox name_cb = new JComboBox();
	
	public DICOM_Classifire() {

		super("DICOM Classifire");
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
		tag_panel.setPreferredSize(new Dimension(190, 300));
		tag_panel.setBorder(new TitledBorder(border, "Available Tags"));
		main_panel.add(tag_panel);
		tag_panel.setLayout(new BoxLayout(tag_panel, BoxLayout.Y_AXIS));
		tagslist = new JList(taglist_model);
		//ダブルクリックで追加
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
		scroll_tagslist.setPreferredSize(new Dimension(190, 200));
		tag_panel.add(scroll_tagslist);
		//search_t.setPreferredSize(new Dimension(190, 20));
		tag_panel.add(search_t);
		// 検索テキストフィールドにKeyListenerを追加
		search_t.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				searchTagsList(taglist_model, uniqueTags); // 検索メソッドを呼び出す
			}
		});
		
		
		JPanel rule_panel = new JPanel();
		rule_panel.setBorder(new TitledBorder(border, "Created Rules"));
		rule_panel.setPreferredSize(new Dimension(420, 300));
		rule_panel.setLayout(new BoxLayout(rule_panel, BoxLayout.X_AXIS));
		main_panel.add(rule_panel);
		// buillding path panel
		JPanel path_panel = new JPanel();
		path_panel.setBorder(new TitledBorder(border, "Path Building"));
		path_panel.setPreferredSize(new Dimension(200, 250));
		//path_tab.setPreferredSize(new Dimension(190, 250));
		rule_panel.add(path_panel);
		// building path tab
		path_panel.add(path_tab);
		// directory panel
		JPanel dir_panel = new JPanel();
		dir_panel.setLayout(new BoxLayout(dir_panel, BoxLayout.Y_AXIS));
		path_tab.add("Directory", dir_panel);
		JList dir_tagslist = new JList(dir_model);
		// dir_tagslistをリストに追加
		setall_tagsLists.add(dir_tagslist);
		JScrollPane scroll_dir_tagslist = new JScrollPane(dir_tagslist);
		scroll_dir_tagslist.setPreferredSize(new Dimension(180, 195));
		dir_panel.add(scroll_dir_tagslist);
		dir_cb.addItem("Layered");
		dir_cb.addItem("Connect Tags");
		dir_panel.add(dir_cb);
		// filename panel
		JPanel name_panel = new JPanel();
		name_panel.setLayout(new BoxLayout(name_panel, BoxLayout.Y_AXIS));
		path_tab.add("FileName", name_panel);
		JList name_tagslist = new JList(name_model);
		// name_tagslistをリストに追加
		setall_tagsLists.add(name_tagslist);
		JScrollPane scroll_name_tagslist = new JScrollPane(name_tagslist);
		scroll_name_tagslist.setPreferredSize(new Dimension(180, 195));
		name_panel.add(scroll_name_tagslist);
		name_cb.addItem("Off");
		name_cb.addItem("SOP Instance UID");
		name_cb.addItem("Connect Tags");
		name_panel.add(name_cb);

		// filter panel
		JPanel filter_panel = new JPanel();
		filter_panel.setBorder(new TitledBorder(border, "Filtering"));
		filter_panel.setPreferredSize(new Dimension(200, 250));
		rule_panel.add(filter_panel);
		// buillding filter tab
		JTabbedPane filter_tab = new JTabbedPane();
		filter_tab.setPreferredSize(new Dimension(185, 250));
		filter_panel.add(filter_tab);
		
		// range panel
		JPanel range_panel = new JPanel();
		range_panel.setLayout(new BoxLayout(range_panel, BoxLayout.Y_AXIS));
		filter_tab.add("Range", range_panel);
		
		JPanel rangeset_panel = new JPanel();
		range_panel.add(rangeset_panel);
		JButton range_b = new JButton("Add");
		range_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addRange();
			}
		});
		rangeset_panel.setPreferredSize(new Dimension(180, 70));
		rangeset_panel.add(min);
		rangeset_panel.add(max);
		rangeset_panel.add(memo);
		rangeset_panel.add(range_b);

		
		JList range_list = new JList(range_model);
		JScrollPane scroll_range_list = new JScrollPane(range_list);
		scroll_range_list.setPreferredSize(new Dimension(180, 160));
		range_panel.add(scroll_range_list);
		JComboBox range_cb = new JComboBox();
		range_cb.addItem("Off");
		range_cb.addItem("Instance Number");
		range_cb.addItem("Slice Location");
		range_panel.add(range_cb);
		// advanced panel
		JPanel advanced_panel = new JPanel();
		advanced_panel.setLayout(new BoxLayout(advanced_panel, BoxLayout.Y_AXIS));
		filter_tab.add("Advanced", advanced_panel);
		JPanel advset_panel = new JPanel();
		advanced_panel.add(advset_panel);
		JButton adv_b = new JButton("Add");
		adv_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addAdvanced();
			}
		});
		advset_panel.setPreferredSize(new Dimension(180, 70));
		advset_panel.add(group);
		advset_panel.add(element);
		advset_panel.add(value);
		advset_panel.add(adv_b);
		
		
		JList advanced_list = new JList(advanced_model);
		JScrollPane scroll_advanced_list = new JScrollPane(advanced_list);
		scroll_advanced_list.setPreferredSize(new Dimension(180, 160));
		advanced_panel.add(scroll_advanced_list);
		JComboBox advanced_cb = new JComboBox();
		advanced_cb.addItem("Off");
		advanced_cb.addItem("And");
		advanced_cb.addItem("Or");
		advanced_panel.add(advanced_cb);
		
		JButton start_b = new JButton("Start");
		io_panel.add(start_b);
		start_b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				classifyImages();
			}
		});
		
	    // ステータスバーの設定
		statusLabel.setBorder(new TitledBorder(border));
	    statusPanel.add(statusLabel);
	    // プログレスバーの設定
	    progressBar.setStringPainted(true); // パーセンテージ表示を有効にする
	    statusPanel.add(progressBar);		

		JPanel panelBase = new JPanel();
		panelBase.setLayout(new BoxLayout(panelBase, BoxLayout.Y_AXIS));
		panelBase.add(io_panel);
		panelBase.add(main_panel);
	    // GUIにステータスバーとプログレスバーを追加
	    panelBase.add(statusPanel, BorderLayout.SOUTH);

	    
	    // バックスペースで要素を削除するキーリスナー
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

	    // 各 JList にキーリスナーを追加
	    dir_tagslist.addKeyListener(listKeyListener);
	    name_tagslist.addKeyListener(listKeyListener);
	    range_list.addKeyListener(listKeyListener);
	    advanced_list.addKeyListener(listKeyListener);

		
		
		
		add(panelBase);
		
		setVisible(true);
	}
	
	// ステータスバーのテキストを更新するメソッド
	private void setStatusText(String text) {
	    statusLabel.setText(text);
	}

	// プログレスバーの値を更新するメソッド
	private void setProgressValue(int value) {
	    progressBar.setValue(value);
	}


	// 他のメソッドと同じようにselect_input()メソッドを定義
	public void select_input() {
		dicomFilePaths = new ArrayList<>();
		String inputDir = IJ.getDirectory("Input directory");
		input_t.setText(inputDir);
		// Find .dcm files in the input directory
		dicomFilePaths = findDICOMFiles(inputDir);
		// リストのコピーを作成し、それをシャッフルする
		List<String> shuffled_path = new ArrayList<>(dicomFilePaths);
		Collections.shuffle(shuffled_path);
		// uniqueTagsをクリアしてからタグを追加する
		uniqueTags.clear();
		for (int i = 0; i < Math.min(shuffled_path.size(), 3); i++) {
			String file_path = shuffled_path.get(i);
			addUniqueTags(file_path, uniqueTags);
		}
		// JList にタグを設定
		taglist_model.clear(); // タグリストをクリア
		for (String tag : uniqueTags) {
			taglist_model.addElement(tag);
		}
	}

	public void select_output() {
		String outputDir = IJ.getDirectory("Output directory");
		output_t.setText(outputDir);
	}

	// Function to recursively find .dcm files in a directory
	private List<String> findDICOMFiles(String directoryPath) {
		findDICOMFilesInDirectory(new File(directoryPath), dicomFilePaths);
		return dicomFilePaths;
	}

	// Recursive function to find .dcm files in a directory and its subdirectories
	private void findDICOMFilesInDirectory(File directory, List<String> dicomFilePaths) {
		// List all files and directories in the current directory
		File[] files = directory.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					// If it's a directory, recursively call this function
					findDICOMFilesInDirectory(file, dicomFilePaths);
				} else {
					imp = IJ.openImage(file.toString());
					// If it's a file, check if it's a .dcm file
					if (file.isFile() && imp != null) {
						// If it's a .dcm file, add its path to the list
						dicomFilePaths.add(file.getAbsolutePath());
					}
				}
			}
		}
	}

	private void addUniqueTags(String filePath, List<String> uniqueTags) {
		DICOM dicom = new DICOM(); // DICOMクラスのインスタンスを作成
		String dcminfo = dicom.getInfo(filePath);
		Pattern pattern = Pattern.compile("(.*?:)");
		Matcher matcher = pattern.matcher(dcminfo);
		while (matcher.find()) {
			String found = matcher.group(1).trim();
			if (!uniqueTags.contains(found)) { // 重複をチェックして追加
				uniqueTags.add(found); // マッチした部分文字列をリストに追加
			}
		}
	}

	public void searchTagsList(DefaultListModel<String> taglist_model, List<String> uniqueTags) {
		List<String> allTags = new ArrayList<>(uniqueTags); // uniqueTagsのコピーを作成
		// フィルターテキストを整形して、大文字と小文字の違いを無視する
		String searach = search_t.getText();
		searach = searach.toLowerCase().replace(" ", "").replace(",", "").replace(":", "");
		// 列挙されたタグをフィルタリングする
		taglist_model.clear(); // 列挙されたタグリストをクリア
		for (String tag : allTags) {
			// タグの文字列を整形して、大文字と小文字の違いを無視して検索する
			String formattedTag = tag.toLowerCase().replaceAll("[ ,:]", "");
			if (formattedTag.contains(searach)) {
				taglist_model.addElement(tag); // フィルターテキストにマッチするタグをリストに追加
			}
		}
	}

	public void addSelectedItemToActiveList(DefaultListModel<String> tagListModel, JTabbedPane tabbedPane) {
	    // 選択されたアイテムを取得
	    List<String> selectedItems = tagslist.getSelectedValuesList();
	    // アクティブなタブのインデックスを取得
	    int selectedIndex = tabbedPane.getSelectedIndex();
	    // 選択されたタブに対応するJListを取得
	    JList<String> activeList = setall_tagsLists.get(selectedIndex);
	    // タブが0番目の場合の処理
	    if (selectedIndex == 0) {
	        for (String selectedItem : selectedItems) {
	            dir_model.addElement(selectedItem);
	        }
	    } else {
	        for (String selectedItem : selectedItems) {
	            name_model.addElement(selectedItem);
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
	        
	        // マイナス値が入力されている場合、minとmaxの大小関係を正しく表記し直す
	        double correctedMinValue = Math.min(minValue, maxValue);
	        double correctedMaxValue = Math.max(minValue, maxValue);
	        
	        String rangeText = String.format("%.2f ~ %.2f : %s", correctedMinValue, correctedMaxValue, memoText);
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
	
    // 他のメソッドと同じように、get_tag_valuesメソッドを定義します。
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
	
	// Classify Images Method
	private void classifyImages() {

	    String outputFolderPath = output_t.getText();
	    List<String> dir_listItems = getpathlistItems(dir_model);
	    List<String> name_listItems = getpathlistItems(name_model);
	    int totalFiles = dicomFilePaths.size();
	    // プログレスバーの最大値を設定
	    progressBar.setMaximum(totalFiles);

	        
	    for (int i = 0; i < totalFiles; i++) {
	        String filePath = dicomFilePaths.get(i);            
	        imp = IJ.openImage(filePath);
	        processDICOMFile(imp, filePath, outputFolderPath, dir_listItems, name_listItems, totalFiles, i);
	    }
	}
	
	

	// Process DICOM File Method
	private void processDICOMFile(ImagePlus imp, String filePath, String outputFolderPath,
			List<String> dirItems, List<String> nameItems, int totalFiles, int currentFileIndex) {
		String subDir = outputFolderPath;
	    String fileName = new File(filePath).getName();
	    

	    // Define a method to get tag values
        List<String> tagValues = new ArrayList<>();
        for (String item : dirItems) {
        	String value = DicomTools.getTag(imp, item).toString().replaceAll("[\\/:*?\"<>|]", "_").replaceAll("\r\n", "").trim();
            tagValues.add(value);
            }
        
	    // Handle directory name setting button cases
	    if (name_cb.getSelectedItem().equals("SOP Instance UID")) {
	        String uid = DicomTools.getTag(imp, "0008,0018").toString();
	        fileName = uid;
	    } else if (name_cb.getSelectedItem().equals("Connect Tags")) {
	    	fileName = String.join("_", tagValues);
	        }

	    // Handle directory dir setting button cases
	    if (dir_cb.getSelectedItem().equals("Layered")) {
	        for (String value : tagValues) {
	            subDir = Paths.get(subDir, value).toString();
	        }
	    } else if (dir_cb.getSelectedItem().equals("Connect Tags")) {
	            subDir = Paths.get(subDir, String.join("_", tagValues)).toString();
	        }
	    
	    // Create new directory if it doesn't exist
	    File newDir = new File(subDir);
	    if (!newDir.exists()) {
	        newDir.mkdirs();
	    }
	    
	    Path newfilePath = Paths.get(newDir.toString(), fileName);
	    Path orifilePath = Paths.get(filePath);
	    
        try {
            // ファイルをコピー
            Files.copy(orifilePath, newfilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
     // プログレスバーの値を更新
        setProgressValue(currentFileIndex + 1);

	}


	public static void main(String[] args) {
		new DICOM_Classifire();
	}
}
