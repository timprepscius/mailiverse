/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_ArrivalsProcessor_h__
#define __mailiverse_mail_manager_ArrivalsProcessor_h__

#include "mailiverse/utilities/Exception.h"
#include "Direction.h"
#include "../model/Mail.h"
#include "Servent.h"
#include "mailiverse/utilities/Json.h"

namespace mailiverse {
namespace mail {
namespace manager {

class ArrivalsProcessor : public Servent
{
protected:
	DECLARE_EXCEPTION(DuplicateMailException);

	String decode(const String &);
	String getFirstHeader(json::Object *message, const String &s, const String &def);
	model::MailPtr parse(Direction::Enum direction, const String &externalKey, const Date &date, const Block &block);

public:
	ArrivalsProcessor();
	virtual ~ArrivalsProcessor();

	bool alreadyProcessed(const String &path);

	void processSuccess (Direction::Enum direction, const String &externalKey, const Date &date, const Block &block);
	model::MailPtr processStream (Direction::Enum direction, const String &externalKey, const Date &date, const Block &block);
	Pair<Date,Date> getRequestDateRange ();

	void processFailure(Direction::Enum direction, const String &path, const Date &date, const Exception &e);
};

DECLARE_SMARTPTR(ArrivalsProcessor);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* __mailiverse_mail_manager_ArrivalsProcessor_h__ */
