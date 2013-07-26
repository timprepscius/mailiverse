/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPClientSessionSerializer_h__
#define __mailiverse_core_srp_SRPClientSessionSerializer_h__

#include "SRPClientSession.h"
#include "mailiverse/Types.h"
#include "mailiverse/utilities/Json.h"
#include "mailiverse/core/crypt/Base64.h"

namespace mailiverse {
namespace core {
namespace srp {

class SRPClientSessionSerializer {

public:

	static std::map<String,String> serialize (const SRPClientSession &srp)
	{
		std::map<String, String> o;
		
		if (!srp.fPassword.empty())
			o["p"] = (json::String)crypt::Base64::encode(srp.fPassword);
		if (!srp.fCommonValue_S.is_zero())
			o["S"] = (json::String)crypt::Base64::encode(toBlock(srp.fCommonValue_S));
		if (!srp.fEvidenceValue_M1.is_zero())
			o["M1"] = (json::String)crypt::Base64::encode(toBlock(srp.fEvidenceValue_M1));
		if (!srp.fPrivateKey_x.is_zero())
			o["x"] = (json::String)crypt::Base64::encode(toBlock(srp.fPrivateKey_x));
		if (!srp.fPublicKey_A.is_zero())
			o["A"] = (json::String)crypt::Base64::encode(toBlock(srp.fPublicKey_A));
		if (!srp.fPublicKey_A.is_zero())
			o["A"] = (json::String)crypt::Base64::encode(toBlock(srp.fPublicKey_A));
		if (!srp.fRandom_a.is_zero())
			o["a"] = (json::String)crypt::Base64::encode(toBlock(srp.fRandom_a));
		if (!srp.fSessionKey_K.empty())
			o["K"] = (json::String)crypt::Base64::encode(srp.fSessionKey_K);
			
		return o;
	}
	
	static void deserialize (std::map<std::string,std::string> o, SRPClientSession &srp)
	{
		if (o.find("p")!=o.end())
			srp.fPassword = crypt::Base64::decode(o["p"]);
		if (o.find("S")!=o.end())
			srp.fCommonValue_S = toBigInteger(crypt::Base64::decode(o["S"]));
		if (o.find("M1")!=o.end())
			srp.fEvidenceValue_M1 = toBigInteger(crypt::Base64::decode(o["M1"]));
		if (o.find("x")!=o.end())
			srp.fPrivateKey_x = toBigInteger(crypt::Base64::decode(o["x"]));
		if (o.find("A")!=o.end())
			srp.fPublicKey_A = toBigInteger(crypt::Base64::decode(o["A"]));
		if (o.find("a")!=o.end())
			srp.fRandom_a = toBigInteger(crypt::Base64::decode(o["a"]));
		if (o.find("K")!=o.end())
			srp.fSessionKey_K = crypt::Base64::decode(o["K"]);
	}
	

} ;

} // namespace 
} // namespace 
} // namespace 

#endif
