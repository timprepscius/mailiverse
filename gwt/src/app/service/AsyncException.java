/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

@Export()
public class AsyncException extends Exception implements Exportable {

	public AsyncException() {
		// TODO Auto-generated constructor stub
	}

	public AsyncException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
