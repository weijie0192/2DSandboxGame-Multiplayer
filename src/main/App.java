package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

/**
 * Hello world!
 */
public final class App extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7276097955257424485L;
	private JTextField txtName;

	public App(String name) {
		this.txtName = new JTextField();
		this.txtName.setText(name);
		this.setup();

	}

	public App() {
		this.txtName = new JTextField();
		this.setup();
	}

	private void setup() {

		this.setTitle("SPBLXS Game");
		
		final Font font = new Font("SansSerif", Font.PLAIN, 20);
		JPanel panel = new JPanel(new BorderLayout(5, 20));
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel lblTitle = new JLabel("<html><h1><i>SPBLXS Multiplayer PVP<i></h1></html>", JLabel.CENTER);
		panel.add(lblTitle, BorderLayout.NORTH);
		JLabel lblName = new JLabel("Name: ");
		lblName.setFont(font);
		panel.add(lblName, BorderLayout.WEST);
		txtName.setFont(font);
		panel.add(txtName, BorderLayout.CENTER);
		JButton joinBtn = new JButton("Join Room");
		panel.setBackground(Color.WHITE);
		joinBtn.setForeground(Color.WHITE);
		joinBtn.setFont(font);
		joinBtn.addActionListener(e->{
			if(!txtName.getText().trim().equals("")) {
				try {
					int port = Integer.parseInt(JOptionPane.showInputDialog("Port Number"));
					new Room(txtName.getText().trim(), port);
					dispose();
				}catch(NumberFormatException ex) {
					JOptionPane.showMessageDialog(null, "Invalid port number");
				}
			}else {
				JOptionPane.showMessageDialog(null, "Please choose a name");
			}
		});
		this.getRootPane().setDefaultButton(joinBtn);
		panel.add(joinBtn, BorderLayout.SOUTH);
		this.add(panel);
		this.setSize(400,235);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // allow x to exit the application
		this.setVisible(true);
	}

	/**
	 * Says hello to the world.
	 * 
	 * @param args The arguments of the program.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			// display an alert window when a packet is received
			try {
				for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
					if ("Nimbus".equals(info.getName())) {
						UIManager.setLookAndFeel(info.getClassName());
						break;
					}
				}
			} catch (Exception e) {
				System.out.println("Error");
			}
			new App();

		});
	}
}
