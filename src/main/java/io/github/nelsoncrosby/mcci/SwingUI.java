package io.github.nelsoncrosby.mcci;

import io.github.nelsoncrosby.swingutils.JTextAreaAppender;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class SwingUI extends JFrame {
    private MCCI app;
    
    private JTextField selectedFile;
    private JComboBox<String> profileBox;
    
    public SwingUI(MCCI appParam) throws HeadlessException {
        super("MCCI");
        this.app = appParam;
        
        /* === WIDGETS === */
        setLayout(new BorderLayout());
        
        JPanel form = new JPanel();
        form.setLayout(new GridLayout(2, 3, 5, 5));
        
        form.add(new JLabel("Content file:"));
        selectedFile = new JTextField();
        form.add(selectedFile);
        JButton selectFileButton = new JButton("Select file...");
        setSelectFileButtonAction(selectFileButton);
        form.add(selectFileButton);
        
        form.add(new JLabel("Profile:"));
        profileBox = new JComboBox<>();
        for (String profile : app.getProfileNames()) {
            profileBox.addItem(profile);
        }
        profileBox.setSelectedItem(app.selectedProfile());
        setProfileBoxItemChangeAction(profileBox);
        form.add(profileBox);
        JButton newProfileButton = new JButton("New profile...");
        setNewProfileButtonAction(newProfileButton);
        form.add(newProfileButton);
        
        add(form, BorderLayout.CENTER);
        
        JPanel buttonBar = new JPanel();
        JButton installButton = new JButton("Install...");
        setInstallButtonAction(installButton);
        buttonBar.add(installButton);
        JButton exitButton = new JButton("Exit");
        setExitButtonAction(exitButton);
        buttonBar.add(exitButton);
        
        add(buttonBar, BorderLayout.SOUTH);
        pack();
        
        
        /* === WINDOW PROPERTIES === */
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }
    
    private void setProfileBoxItemChangeAction(JComboBox<String> profileBox) {
        profileBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                String profile = (String) e.getItem();
                app.selectProfile(profile);
            }
        });
    }
    
    private void setSelectFileButtonAction(JButton selectFileButton) {
        final SwingUI self = this;
        selectFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Select content");
                fileChooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public boolean accept(File f) {
                        return f.isFile() &&
                                (f.getName().endsWith(".zip") || f.getName().endsWith(".jar"));
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }
                });
                fileChooser.showOpenDialog(self);
                File selected = fileChooser.getSelectedFile();
                if (selected != null)
                    selectedFile.setText(selected.getAbsolutePath());
            }
        });
    }
    
    private void setNewProfileButtonAction(JButton newProfileButton) {
        newProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JFrame dialog = new JFrame("Create new profile");
                dialog.setLayout(new BorderLayout());
                
                JPanel form = new JPanel(new GridLayout(2, 2));
                
                form.add(new JLabel("Profile name:"));
                final JTextField nameField = new JTextField();
                form.add(nameField);
                
                form.add(new JLabel("Profile folder:"));
                final JTextField dirField = new JTextField();
                form.add(dirField);
                
                dialog.add(form, BorderLayout.CENTER);
                
                JPanel buttonPanel = new JPanel(new FlowLayout());
                
                JButton createButton = new JButton("Create");
                createButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        String name = nameField.getText();
                        String dir = dirField.getText();
                        
                        if (name == null || name.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog,
                                    "You must specify a name", "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        if (app.getProfileNames().contains(name)) {
                            JOptionPane.showMessageDialog(dialog,
                                    "That profile alroady exists", "Invalid input",
                                    JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        
                        if (dir == null || dir.isEmpty()) {
                            JOptionPane.showMessageDialog(dialog,
                                    "No directory provided - using .minecraft/" + name, "Adjusting input",
                                    JOptionPane.INFORMATION_MESSAGE);
                            dir = name;
                        }
                        
                        File file = new File(dir);
                        if (!file.isAbsolute()) {
                            file = new File(LauncherConfig.DOT_MINECRAFT, file.getPath());
                        }
                        
                        file.mkdirs();
                        
                        app.newProfile(name, file);
                        app.selectProfile(name);

                        profileBox.removeAllItems();
                        for (String profile : app.getProfileNames()) {
                            profileBox.addItem(profile);
                        }
                        profileBox.setSelectedItem(name);
                        
                        dialog.dispose();
                    }
                });
                buttonPanel.add(createButton);
                
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                });
                buttonPanel.add(cancelButton);
                
                dialog.add(buttonPanel, BorderLayout.SOUTH);
                
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
    }
    
    private void setInstallButtonAction(JButton installButton) {
        installButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                File contentFile = new File(selectedFile.getText());
                final JFrame loggingDialog = new JFrame("Installing content...");
                loggingDialog.setLayout(new BorderLayout());
                
                JTextArea loggingConsole = new JTextArea(24, 64);
                loggingConsole.setFont(new Font("Courier New", 0, 12));
                ((DefaultCaret) loggingConsole.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
                loggingConsole.setEditable(false);
                loggingConsole.setLineWrap(true);
                loggingConsole.setWrapStyleWord(true);
                
                JScrollPane scrollPane = new JScrollPane(loggingConsole);
                loggingDialog.add(scrollPane, BorderLayout.CENTER);
                
                loggingDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                loggingDialog.pack();
                loggingDialog.setLocationRelativeTo(null);
                loggingDialog.setVisible(true);

                JTextAreaAppender msgLog = new JTextAreaAppender(loggingConsole);
                try {
                    app.installContentToSelectedProfile(contentFile, msgLog);
                } catch (IOException e) {
                    try {
                        msgLog.append("Error: ").append(e.getMessage())
                            .append(" (").append(e.getClass().getName()).append(')');
                    } catch (IOException e1) {
                        throw new Error("Should never happen (because our " +
                                "appender never throws this)", e1);
                    }
                } catch (Content.UnsupportedContentTypeException e) {
                    msgLog.append(e.getMessage());
                }
                
                JButton doneButton = new JButton("Done");
                doneButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        loggingDialog.dispose();
                    }
                });
                loggingDialog.add(doneButton, BorderLayout.SOUTH);
                loggingDialog.pack();
            }
        });
    }

    private void setExitButtonAction(JButton exitButton) {
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
}
