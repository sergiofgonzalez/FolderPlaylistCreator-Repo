package org.joolzminer.fpc.swingui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
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

	private static final Logger LOGGER = LoggerFactory
			.getLogger(FolderPlaylistCreator.class);

	private final JButton selectFolderButton = new JButton("Browse");
	private final JTextField selectedFolderTextField = new JTextField(
			"[none selected]");
	private JTextArea folderContentsTextArea = new JTextArea(
			"[no folder selected]");
	private JButton createButton = new JButton("Create");
	private JScrollPane scrollPane;
	private JMenuItem fileCreateMenuItem;

	private DirFilesReader dirFilesReader;
	private PlaylistWriter playlistWriter;
	private Collection<Path> playlistFiles;

	private class SelectFolderAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			fileChooser.setDialogTitle("Select Folder");
			int returnValue = fileChooser.showOpenDialog(null);
			if (returnValue == JFileChooser.APPROVE_OPTION) {
				selectedFolderTextField.setText(fileChooser.getSelectedFile()
						.getAbsolutePath());

				try {
					playlistFiles = dirFilesReader.read(Paths
							.get(selectedFolderTextField.getText()));
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(null,
							"Could not read selected folder", "Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				if (playlistFiles.isEmpty()) {
					JOptionPane.showMessageDialog(null,
							"Selected folder is Empty", "Warning",
							JOptionPane.WARNING_MESSAGE);
					return;
				}
				folderContentsTextArea.setText(null);
				for (Path fileInFolder : playlistFiles) {
					folderContentsTextArea.setText(folderContentsTextArea
							.getText() + fileInFolder + "\n");
				}

				createButton.setEnabled(true);
				fileCreateMenuItem.setEnabled(true);
				revalidate();
			}
		}
	}

	private class CreatePlaylistAction implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			Path playlistFilename = Paths.get(selectedFolderTextField.getText()
					+ ".m3u");
			LOGGER.debug("playlistFilename={}", playlistFilename);
			if (Files.exists(playlistFilename)) {
				Path onlyFilename = Paths.get(playlistFilename.toString())
						.getFileName();
				int response = JOptionPane.showConfirmDialog(null, onlyFilename
						+ "\nalready exists.\n Do you want to replace it?",
						"Confirm Overwrite", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.NO_OPTION) {
					return;
				}
			}
			playlistWriter.write(playlistFilename, playlistFiles);
			JOptionPane.showMessageDialog(null,
					Paths.get(playlistFilename.toString()).getFileName()
							+ " created.", "Information",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public FolderPlaylistCreator(String title) {
		super(title);
		init();
	}

	private void init() {
		@SuppressWarnings("resource")
		ApplicationContext context = new AnnotationConfigApplicationContext(
				ApplicationConfig.class);
		dirFilesReader = context.getBean(DirFilesReader.class);
		playlistWriter = context.getBean(PlaylistWriter.class);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		setLayout(new FlowLayout());

		selectFolderButton.setIcon(new ImageIcon(
				"target/classes/icons/audio-folder-icon-32x32.png"));
		selectFolderButton.addActionListener(new SelectFolderAction());

		selectedFolderTextField.setEditable(false);
		selectedFolderTextField.setPreferredSize(new Dimension(400, 25));

		scrollPane = new JScrollPane(folderContentsTextArea,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		scrollPane.setPreferredSize(new Dimension(550, 500));

		createButton.setEnabled(false);

		createButton.addActionListener(new CreatePlaylistAction());

		// adding all components
		add(selectFolderButton);
		add(selectedFolderTextField);
		add(scrollPane);
		add(createButton);

		initMenu();
	}

	private void initMenu() {
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu helpMenu = new JMenu("Help");

		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(helpMenu);

		JMenuItem fileNewMenuItem = new JMenuItem("New");
		JMenuItem fileOpenMenuItem = new JMenuItem("Open...", new ImageIcon(
				"target/classes/icons/audio-folder-icon-16x16.png"));
		fileOpenMenuItem.addActionListener(new SelectFolderAction());
		fileCreateMenuItem = new JMenuItem("Create");
		fileCreateMenuItem.setEnabled(false);
		fileCreateMenuItem.addActionListener(new CreatePlaylistAction());
		JMenuItem fileExitMenuItem = new JMenuItem("Exit");
		fileExitMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				confirmExitApp();
			}
		});

		fileNewMenuItem.setEnabled(false);
		fileMenu.add(fileNewMenuItem);
		fileMenu.add(fileOpenMenuItem);
		fileMenu.add(fileCreateMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(fileExitMenuItem);

		JMenuItem cutMenuItem = new JMenuItem("Cut");
		cutMenuItem.setEnabled(false);
		JMenuItem copyMenuItem = new JMenuItem("Copy");
		copyMenuItem.setEnabled(false);
		JMenuItem pasteMenuItem = new JMenuItem("Paste");
		pasteMenuItem.setEnabled(false);

		editMenu.add(cutMenuItem);
		editMenu.add(copyMenuItem);
		editMenu.add(pasteMenuItem);

		JMenuItem aboutMenuItem = new JMenuItem(
				"About FolderPlaylistCreator...");
		helpMenu.add(aboutMenuItem);
		aboutMenuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane
						.showMessageDialog(
								null,
								"FolderPlaylistCreator v1.0-SNAPSHOT",
								"About FolderPlaylistCreator",
								JOptionPane.OK_OPTION,
								new ImageIcon(
										"target/classes/icons/audio-folder-icon-128x128.png"));
			}
		});

		setJMenuBar(menuBar);
	}

	private static void constructGUI() {
		JFrame.setDefaultLookAndFeelDecorated(true);
		FolderPlaylistCreator frame = new FolderPlaylistCreator(
				"Folder Playlist Creator");
		frame.setSize(575, 660);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setIconImage(new ImageIcon(
				"target/classes/icons/audio-folder-icon-32x32.png").getImage());
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				confirmExitApp();
			}
		});
		frame.setVisible(true);
	}

	public static void confirmExitApp() {
		int userOption = JOptionPane.showConfirmDialog(null,
				"Exit FolderPlaylistCreator?", "Exit Application",
				JOptionPane.YES_NO_OPTION);
		if (userOption == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}

	public static void setupGlobalExceptionHandling() {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				JOptionPane.showMessageDialog(null,
						"Unexpected error occurred:\n" + e, "Error",
						JOptionPane.ERROR_MESSAGE);
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
