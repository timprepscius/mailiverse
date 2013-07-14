/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.exceptions;

import java.io.IOException;

public class UserExistsException extends PublicMessageException
{
	public UserExistsException ()
	{
		super("User already exists");
	}
}
