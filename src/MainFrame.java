import google.Searcher;
import google.Downloader;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    Container pane = getContentPane();


    // <variable>
    int searchIndex = 1;

    JPanel pan = new JPanel();
    JLabel searchLabel = new JLabel("검색어");
    JTextField searchField = new JTextField(20);
    JLabel searchTypeLabel = new JLabel("파일확장자 : ");
    JTextField searchTypeField = new JTextField(5);
    JButton searchButton = new JButton("검색");
    JLabel nowIndex = new JLabel("현재 페이지 : 0");

    String colName[] = {" ", "Title", "Size", "URL"};
    DefaultTableModel t_model = new DefaultTableModel(colName, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            if (column == 0) //CheckBox's column
                return true;
            else
                return false;
        }
    };

    JTable table = new JTable(t_model) {
        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0: //CheckBox
                    return Boolean.class;
                default:
                    return String.class;
            }
        }
    };

    JPanel pan2 = new JPanel();
    JButton prevButton = new JButton("이전 페이지");
    JButton nextButton = new JButton("다음 페이지");
    JButton selectDownButton = new JButton("선택 다운");
    JButton AllDownButton = new JButton("현재 페이지 다운");

    JProgressBar loadingBar = new JProgressBar(0, 100);
    // </variable>

    // <function>
    void clearRow() {
        for (int i = t_model.getRowCount(); i > 0; i--) {
            t_model.removeRow(i - 1);
        }
    }

    void loadingInit() {
        loadingBar.setString("Loading...");
        loadingBar.setValue(0);
    }

    void loadingComplete() {
        loadingBar.setString("Complete");
    }

    void searchAndAddRow() {
        clearRow();

        String fileName = searchField.getText();
        String fileType = searchTypeField.getText();

        try {
            final HashMap<String, String> result = Searcher.Search(fileName, fileType, searchIndex);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    loadingInit();

                    Object[] row = new Object[4];
                    int loadingCount = 0;
                    for (Map.Entry<String, String> e : result.entrySet()) {
                        String t_fileName = e.getKey();
                        String t_fileURL = e.getValue();

                        row[0] = false;
                        row[1] = t_fileName;
                        row[2] = Downloader.getFileSize(t_fileURL);
                        row[3] = t_fileURL;

                        t_model.addRow(row);

                        loadingCount++;
                        int loading = (int) ((double) loadingCount / Searcher.PRINT_PAGES_NUM * 100);
                        loadingBar.setValue(loading);
                    }

                    loadingComplete();
                }
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
        }

        nowIndex.setText("현재 페이지 : " + searchIndex);
    }

    void Download() {
        final String saveDir = getSaveDir() + File.separator;
        System.out.println(saveDir);

        int downCount = 0;
        for (int i = 0; i < table.getRowCount(); i++) {
            if ((Boolean) table.getValueAt(i, 0))
                downCount++;
        }
        final int finalDownCount = downCount;

        new Thread(new Runnable() {
            @Override
            public void run() {
                loadingInit();
                int loadingCount = 0;
                for (int i = 0; i < table.getRowCount(); i++) {
                    if ((Boolean) table.getValueAt(i, 0)) {
                        String fileName = (String) table.getValueAt(i, 1); // Title
                        String fileURL = (String) table.getValueAt(i, 3); // URL

                        Downloader.getFileFromUrl(fileName, fileURL, saveDir);

                        loadingCount++;
                        int percent = (int) ((double) loadingCount / finalDownCount * 100);
                        loadingBar.setValue(percent);
                    }
                }
                loadingComplete();
            }
        }).start();
    }

    String getSaveDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(new FileNameExtensionFilter("폴더", "."));
        chooser.showOpenDialog(null);

        File getDir = chooser.getSelectedFile();
        String dir;

        if (getDir == null)
            dir = "";
        else
            dir = getDir.getPath();

        return dir;
    }

    void checkAllBox() {
        for (int i = 0; i < table.getRowCount(); i++) {
            table.setValueAt(true, i, 0); // CheckBox
        }
    }
    // </function>

    // <constructor>
    MainFrame() {
        setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        setSize(700, 400);
        setTitle("AllDown");

        // <ActionListeners>
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchIndex = 0;
                searchAndAddRow();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchIndex += 1;
                searchAndAddRow();
            }
        });
        prevButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (searchIndex == 0)
                    return;
                searchIndex -= 1;
                searchAndAddRow();
            }
        });
        selectDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Download();
            }
        });

        AllDownButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                checkAllBox();
                Download();
            }
        });
        // </ActionListeners>

        table.getColumn(" ").setPreferredWidth(5);
        table.getColumn("Title").setPreferredWidth(360);
        table.getColumn("Size").setPreferredWidth(80);
        table.getColumn("URL").setPreferredWidth(700 - 360 - 80 - 5);

        loadingInit();
        loadingBar.setStringPainted(true);

        pan.add(searchLabel);
        pan.add(searchField);
        pan.add(searchTypeLabel);
        pan.add(searchTypeField);
        pan.add(searchButton);

        pan2.add(prevButton);
        pan2.add(nowIndex);
        pan2.add(nextButton);
        pan2.add(selectDownButton);
        pan2.add(AllDownButton);
        pan2.add(loadingBar);

        pane.add(pan, BorderLayout.NORTH);
        pane.add(new JScrollPane(table), BorderLayout.CENTER);
        pane.add(pan2, BorderLayout.SOUTH);
        setVisible(true);
    }
    // </constructor>
}
