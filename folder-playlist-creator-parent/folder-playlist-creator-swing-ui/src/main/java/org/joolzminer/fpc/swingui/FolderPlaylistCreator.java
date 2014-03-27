package org.joolzminer.fpc.swingui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ThreadFactory;

import javax.inject.Inject;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.joolzminer.fpc.ApplicationConfig;
import org.joolzminer.fpc.service.DirFilesReader;
import org.joolzminer.fpc.service.PlaylistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class FolderPlaylistCreator extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FolderPlaylistCreator.class);
	
	private final JButton selectFolderButton = new JButton("Browse");
	private final JTextField selectedFolderTextField = new JTextField("[none selected]");
	private JTextArea folderContentsTextArea = new JTextArea("[no folder selected]");
	private JButton createButton = new JButton("Create");
	private JScrollPane scrollPane;
	
	
	private DirFilesReader dirFilesReader;	
	private PlaylistWriter playlistWriter;
	private Collection<Path> playlistFiles;
	
	
	public FolderPlaylistCreator(String title) {
		super(title);
		init();
	}
	
	private void init() {
		ApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
		dirFilesReader = context.getBean(DirFilesReader.class);
		playlistWriter = context.getBean(PlaylistWriter.class);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
		setLayout(new FlowLayout());
		
		selectFolderButton.setIcon(new ImageIcon("target/classes/icons/audio-folder-icon-32x32.png"));
		selectFolderButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				fileChooser.setDialogTitle("Select Folder");
				int returnValue = fileChooser.showOpenDialog(null);
				if (returnValue == JFileChooser.APPROVE_OPTION) {
					selectedFolderTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
					
					try {
						playlistFiles = dirFilesReader.read(Paths.get(selectedFolderTextField.getText()));
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(null, "Could not read selected folder", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if (playlistFiles.isEmpty()) {
						JOptionPane.showMessageDialog(null, "Selected folder is Empty", "Warning", JOptionPane.WARNING_MESSAGE);
						return;
					}
					folderContentsTextArea.setText(null);
					for (Path fileInFolder : playlistFiles) {
						folderContentsTextArea.setText(folderContentsTextArea.getText() + fileInFolder + "\n");
					}
					
					createButton.setEnabled(true);
					revalidate();
				}
			}
		});
	
		selectedFolderTextField.setEditable(false);
		selectedFolderTextField.setPreferredSize(new Dimension(400, 25));
		
		scrollPane = new JScrollPane(folderContentsTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		scrollPane.setPreferredSize(new Dimension(550, 500));
		
		createButton.setEnabled(false);	
		
		createButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Path playlistFilename = Paths.get(selectedFolderTextField.getText() + ".m3u");
				LOGGER.debug("playlistFilename={}", playlistFilename);
				playlistWriter.write(playlistFilename, playlistFiles);
				JOptionPane.showMessageDialog(null, "Playlist created.", "Information", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		
		// adding all components
		add(selectFolderButton);
		add(selectedFolderTextField);
		add(scrollPane);
		add(createButton);
		
				
	}
	
	private static void constructGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		FolderPlaylistCreator frame = new FolderPlaylistCreator("Folder Playlist Creator");
		frame.setSize(575, 660);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
	}
	
	public static void setupGlobalExceptionHandling() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread t, Throwable e) {
					JOptionPane.showMessageDialog(null, "Unexpected error occurred:\n" + e, "Error", JOptionPane.ERROR_MESSAGE);
					LOGGER.error("Unexpected error occurred:", e);
			}
		});
	}
	
	public static void main(String[] args) {		
		
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				setupGlobalExceptionHandling();
				constructGUI();
			}
		});
	}
}
