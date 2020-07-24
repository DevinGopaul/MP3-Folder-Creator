import java.io.*;
import java.awt.event.*;
import javax.swing.*;

public class MP3FolderListener implements ActionListener {
	private MP3Mover mover;
	private JLabel progressLabel;
	private JLabel status;
	private JFrame frame;
	public MP3FolderListener(JLabel progressLabel, JLabel status, JFrame frame) {
		this.status = status;
		this.mover = new MP3Mover();
		this.progressLabel = progressLabel;
		this.frame = frame;
	}
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		System.out.println(e.getActionCommand());
		if (e.getActionCommand() == "Choose folder") {
			this.progressLabel.setText("Working...");
            int returnVal = fc.showOpenDialog(null);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
			   int returnCode = this.mover.moveFile(file.getPath());
			   this.progressLabel.setText("Done");
			   if (returnCode > 1){
				   this.status.setText(""+returnCode+" MP3 files were not put into directories");
			   }
			   else if (returnCode == 1){
				this.status.setText("1 MP3 file was not put into a directory");
				}
			   else {
				this.status.setText("All MP3 files were put into directories");
			   }
			   frame.pack();
            } else {
				this.progressLabel.setText("Ready to perform");
				this.status.setText("---");
				frame.pack();
            }  
        }
	}
}
