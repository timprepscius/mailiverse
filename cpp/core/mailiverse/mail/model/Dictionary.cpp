/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Dictionary.h"
#include "Mail.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::utilities;

Dictionary::WordComparator Dictionary::wordComparator;

Dictionary *Dictionary::add (Mail *mail)
{
	if (mail->getHeader()->getAuthor())
		add (mail->getHeader()->getAuthor()->toString());

	if (mail->getHeader()->getRecipients())
		for (auto &i : mail->getHeader()->getRecipients()->getAll())
			add(i->toString());

	if (mail->getBody()->hasText())
		add (mail->getBody()->calculateTextWithoutReply());

	if (mail->getHeader()->getSubject())
		add (*mail->getHeader()->getSubject());

	return this;
}
