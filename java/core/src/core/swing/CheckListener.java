/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.swing;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class CheckListener implements DocumentListener
{
	Checker checker;
	
	public CheckListener (Checker checker)
	{
		this.checker = checker;
	}
	
	public void onChange ()
	{
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {	checker.onCheck(); }
		});
	}
	
	public void changedUpdate(DocumentEvent e) { onChange(); }
	public void insertUpdate(DocumentEvent e) { onChange(); }
	public void removeUpdate(DocumentEvent e) { onChange(); }
}
