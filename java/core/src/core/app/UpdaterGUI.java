/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.app;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.SpringLayout;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class UpdaterGUI extends JFrame
{

	private JPanel contentPane;
	String description;
	String url;
	private JTextArea txtrDescription;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args)
	{
		run("None", "None");
	}

	public static void run (final String description, final String url)
	{
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				try
				{
					UpdaterGUI frame = new UpdaterGUI();
					frame.bind(description, url);
					frame.setVisible(true);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the frame.
	 */
	public UpdaterGUI()
	{
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		SpringLayout sl_contentPane = new SpringLayout();
		contentPane.setLayout(sl_contentPane);
		
		JButton btnOpenDownloadsPage = new JButton("Open Downloads Page");
		btnOpenDownloadsPage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				onOpen();
			}
		});
		sl_contentPane.putConstraint(SpringLayout.HORIZONTAL_CENTER, btnOpenDownloadsPage, 0, SpringLayout.HORIZONTAL_CENTER, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, btnOpenDownloadsPage, -5, SpringLayout.SOUTH, contentPane);
		contentPane.add(btnOpenDownloadsPage);

		JScrollPane scrollPane = new JScrollPane();
		sl_contentPane.putConstraint(SpringLayout.NORTH, scrollPane, 5, SpringLayout.NORTH, contentPane);
		sl_contentPane.putConstraint(SpringLayout.WEST, scrollPane, 5, SpringLayout.WEST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.EAST, scrollPane, -5, SpringLayout.EAST, contentPane);
		sl_contentPane.putConstraint(SpringLayout.SOUTH, scrollPane, -5, SpringLayout.NORTH, btnOpenDownloadsPage);
		contentPane.add(scrollPane);
		
		txtrDescription = new JTextArea();
		txtrDescription.setText("Description");
		scrollPane.setViewportView(txtrDescription);
	}
	
	public void bind (String description, String url)
	{
		this.description = description;
		txtrDescription.setText(description);
		
		this.url = url;
	}
	
	public void onOpen ()
	{
		UserUtil.openPageInDefaultBrowser(url);
	}
}
