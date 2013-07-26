/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "TransportState.h"

using namespace mailiverse::mail::model;
using namespace mailiverse;

const String
	TransportState::RECEIVED = "RECEIVED",
	TransportState::SENT = "SENT",
	TransportState::DRAFT = "DRAFT",
	TransportState::SENDING = "SENDING",
	TransportState::TRASH = "TRASH",
	TransportState::SPAM = "SPAM",
	TransportState::READ = "READ",
	TransportState::ATTACHMENT = "ATTACHMENT",
	TransportState::DELIMITER = ",";

