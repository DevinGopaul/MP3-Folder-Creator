import javax.swing.*;
import java.awt.*;

public class MP3Folder {
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

	public static void createAndShowGUI() {
		JFrame frame = new JFrame("MP3 Folder Creator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

		JPanel topPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		
		bottomPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

		JButton button;

		JLabel status = new JLabel("---");

		button = new JButton("Choose folder");
		JLabel progressLabel = new JLabel("Ready to perform");

		topPanel.add(button);
		topPanel.add(progressLabel);

		bottomPanel.add(status);

		frame.getContentPane().add(topPanel);
		frame.getContentPane().add(bottomPanel);

		MP3FolderListener listener = new MP3FolderListener(progressLabel, status, frame);

		button.addActionListener(listener);

		frame.pack();

		frame.setVisible(true);

	}
}
