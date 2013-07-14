/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.swing;

import java.awt.TextArea;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class RedirectStreams
{
	JTextArea textArea;

	PrintStream savedOutput;
	PrintStream savedError;
	PrintStream out;
	
	public RedirectStreams(JTextArea textArea)
	{
		this.textArea = textArea;
		OutputStream os = 
			new OutputStream() {
				@Override
				public void write(int b) throws IOException
				{
					updateTextArea(String.valueOf((char) b));
				}

				@Override
				public void write(byte[] b, int off, int len) throws IOException
				{
					updateTextArea(new String(b, off, len));
				}

				@Override
				public void write(byte[] b) throws IOException
				{
					write(b, 0, b.length);
				}
			};
			
		out = new PrintStream(os, true);
	}

	private void updateTextArea(final String text)
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run()
			{
				textArea.append(text);
			}
		});
	}

	public void redirectSystem()
	{
		savedOutput = System.out;
		savedError = System.err;
		
		System.setOut(out);
		System.setErr(out);
	}
	
	public PrintStream getOut ()
	{
		return out;
	}
	
	public void restoreSystem ()
	{
		System.setOut(savedOutput);
		System.setErr(savedError);
	}

}
