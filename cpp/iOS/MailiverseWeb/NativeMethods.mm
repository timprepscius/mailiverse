//
//  NativeMethods.m
//  Mailiverse
//
//  Created by Timothy Prepscius on 1/23/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import "NativeMethods.h"

#include <mailiverse/core/BlockCompression.h>
#include <mailiverse/core/crypt/Base64.h>
#include <mailiverse/core/crypt/PBE.h>
#include <mailiverse/core/crypt/CryptorAES.h>
#include <mailiverse/core/crypt/CryptorRSA.h>
#include <mailiverse/core/srp/SRPFactory.h>
#include <mailiverse/core/srp/SRPClientSession.h>
#include <mailiverse/core/srp/SRPClientSessionSerializer.h>
#include <mailiverse/utilities/Json.h>

using namespace mailiverse::core::srp;
using namespace mailiverse::core::crypt;
using namespace mailiverse::core;
using namespace mailiverse;

@implementation NativeMethods

+(id)processRequest:(NSString *)cmd_ withArgs:(NSArray *)args
{
	@try
	{
//		NSLog(@"processRequest %@", cmd_);
		std::string cmd = cmd_.UTF8String;
		if (cmd == "ping")
		{
			return @"ping";
		}
		else
		if (cmd == "zip_inflate")
		{
			return [NativeMethods zipInflate:[args objectAtIndex:0]];
		}
		else 
		if (cmd == "zip_deflate")
		{
			return [NativeMethods zipDeflate:[args objectAtIndex:0]];
		}
		if (cmd == "pbe_genKey")
		{
			int arg=0;
			NSString *password = [args objectAtIndex:arg++];
			NSString *salt = [args objectAtIndex:arg++];
			NSNumber *iterations = [args objectAtIndex:arg++];
			NSNumber *keyLength = [args objectAtIndex:arg++];
			
			return [NativeMethods pbeKeyGen:password salt64:salt iterations:iterations.intValue keyLength:keyLength.intValue];
		}
		else
		if (cmd == "aes_decrypt")
		{
			int arg=0;
			NSString *key = [args objectAtIndex:arg++];
			NSString *salt = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods aesDecrypt:data withKey:key andSalt:salt];
		}
		else
		if (cmd == "aes_encrypt")
		{
			int arg=0;
			NSString *key = [args objectAtIndex:arg++];
			NSString *salt = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods aesEncrypt:data withKey:key andSalt:salt];
		}
		else
		if (cmd == "rsa_decrypt_serialized_key")
		{
			int arg=0;
			NSString *key = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods rsaDecrypt:data withKey:key];
			
		}
		else
		if (cmd == "rsa_encrypt_serialized_key")
		{
			int arg=0;
			NSString *key = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods rsaEncrypt:data withKey:key];
		}
		else
		if (cmd == "srp_client_setSalt")
		{
			int arg=0;
			NSDictionary *client = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods srpClient:client setSalt:data];
		}
		else
		if (cmd == "srp_client_setServerPublicKey")
		{
			int arg=0;
			NSDictionary *client = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods srpClient:client setServerPublicKey:data];
		}
		else
		if (cmd == "srp_client_validateServerEvidence")
		{
			int arg=0;
			NSDictionary *client = [args objectAtIndex:arg++];
			NSString *data = [args objectAtIndex:arg++];
			
			return [NativeMethods srpClient:client validateServerEvidence:data];
		}
		else
		if (cmd == "cout")
		{
			NSLog(@"%@", [args objectAtIndex:0]);
			return nil;
		}
		
		return @"Unknown";
	}
	@catch (NSException *e)
	{
		return e.reason;
	}
	
}

+(NSString *)zipInflate:(NSString *)in64
{
	std::string out64 = Base64::encode(inflate(Base64::decode(in64.UTF8String)));
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)zipDeflate:(NSString *)in64
{
	std::string out64 = Base64::encode(deflate(Base64::decode(in64.UTF8String)));
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)pbeKeyGen:(NSString *)password salt64:(NSString *)salt64 iterations:(int)iterations keyLength:(int)keyLength
{
	PBE pbe(password.UTF8String, Base64::decode(salt64.UTF8String), iterations, keyLength);
	std::string out64 = Base64::encode(pbe.secretKey);
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)aesEncrypt:(NSString *)data64 withKey:(NSString *)key64 andSalt:(NSString *)salt64
{
	CryptorAES cryptor(Base64::decode(key64.UTF8String), Base64::decode(salt64.UTF8String));
	Block inBytes = Base64::decode(data64.UTF8String);
	std::string out64 = Base64::encode(cryptor.encrypt(inBytes.data(), inBytes.data() + inBytes.size()));
	
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)aesDecrypt:(NSString *)data64 withKey:(NSString *)key64 andSalt:(NSString *)salt64
{
	CryptorAES cryptor(Base64::decode(key64.UTF8String), Base64::decode(salt64.UTF8String));
	Block inBytes = Base64::decode(data64.UTF8String);
	std::string out64 = Base64::encode(cryptor.decrypt(inBytes.data(), inBytes.data() + inBytes.size()));
	
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)rsaEncrypt:(NSString *)data64 withKey:(NSString *)key64
{
	CryptorRSA cryptor(Key(key64.UTF8String), Key());
	Block inBytes = Base64::decode(data64.UTF8String);
	std::string out64 = Base64::encode(cryptor.encryptRSABlock(inBytes));
	
	return [NSString stringWithUTF8String:out64.c_str()];
}

+(NSString *)rsaDecrypt:(NSString *)data64 withKey:(NSString *)key64
{
	CryptorRSA cryptor(Key(), Key(key64.UTF8String));
	Block inBytes = Base64::decode(data64.UTF8String);
	std::string out64 = Base64::encode(cryptor.decryptRSABlock(inBytes));
	
	return [NSString stringWithUTF8String:out64.c_str()];
}

std::map<std::string,std::string> convertDict(NSDictionary *dict)
{
	std::map<std::string, std::string> o;
	
	NSEnumerator *enumerator = [dict keyEnumerator];
	id _key;
	while ((_key = [enumerator nextObject])) {
		NSString *key = _key;
		NSString *value = [dict objectForKey:key];
		
		o[key.UTF8String] = value.UTF8String;
	}

	return o;
}

NSDictionary *convertMapToDict(const std::map<std::string,std::string> &m)
{
	NSMutableDictionary *dict = [[NSMutableDictionary alloc] init];
	for (auto i : m)
	{
		[dict 
			setValue:[NSString stringWithUTF8String:(i.second.c_str())] 
			forKey:[NSString stringWithUTF8String:(i.first.c_str())]
		];
	}
	
	return dict;
}

+(NSDictionary *)srpClient:(NSDictionary *)state setSalt:(NSString *)salt
{
	SRPClientSessionPtr srp = SRPFactory::getInstance()->newClientSession();
	SRPClientSessionSerializer::deserialize(convertDict(state), *srp);
	
	srp->setSalt_s(toBigInteger(Base64::decode(salt.UTF8String)));
	
	return convertMapToDict(SRPClientSessionSerializer::serialize(*srp));
}

+(NSDictionary *)srpClient:(NSDictionary *)state setServerPublicKey:(NSString *)publicKey
{
	SRPClientSessionPtr srp = SRPFactory::getInstance()->newClientSession();
	SRPClientSessionSerializer::deserialize(convertDict(state), *srp);
	
	srp->setServerPublicKey_B(toBigInteger(Base64::decode(publicKey.UTF8String)));
	
	return convertMapToDict(SRPClientSessionSerializer::serialize(*srp));
}

+(NSDictionary *)srpClient:(NSDictionary *)state validateServerEvidence:(NSString *)evidence
{
	SRPClientSessionPtr srp = SRPFactory::getInstance()->newClientSession();
	SRPClientSessionSerializer::deserialize(convertDict(state), *srp);
	
	srp->validateServerEvidenceValue_M2(toBigInteger(Base64::decode(evidence.UTF8String)));
	
	return convertMapToDict(SRPClientSessionSerializer::serialize(*srp));
}

@end
