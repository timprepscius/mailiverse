//
//  NativeMethods.h
//  Mailiverse
//
//  Created by Timothy Prepscius on 1/23/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface NativeMethods : NSObject

+(id)processRequest:(NSString *)cmd withArgs:(NSArray *)args;

+(NSString *)zipInflate:(NSString *)in64;
+(NSString *)zipDeflate:(NSString *)in64;

+(NSString *)pbeKeyGen:(NSString *)password salt64:(NSString *)salt64 iterations:(int)iterations keyLength:(int)keyLength;

+(NSString *)aesEncrypt:(NSString *)data64 withKey:(NSString *)key64 andSalt:(NSString *)salt64;
+(NSString *)aesDecrypt:(NSString *)data64 withKey:(NSString *)key64 andSalt:(NSString *)salt64;

+(NSString *)rsaEncrypt:(NSString *)data64 withKey:(NSString *)key64;
+(NSString *)rsaDecrypt:(NSString *)data64 withKey:(NSString *)key64;

@end
