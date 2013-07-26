/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CryptorRSAAES.h"

using namespace mailiverse::core::crypt;

const CryptorRSAAES::Version 
	CryptorRSAAES::Versions::R2012 = 0x00,
	CryptorRSAAES::Versions::R201303 = 0x01,
	CryptorRSAAES::Versions::CURRENT = Versions::R201303;
