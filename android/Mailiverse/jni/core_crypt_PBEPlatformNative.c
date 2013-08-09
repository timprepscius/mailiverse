#include "core_crypt_PBEPlatformNative.h"

// downloaded from https://github.com/johanns/PBKDF256/tree/master/ext/core
#include "sha256.h"
#include <stdlib.h>

JNIEXPORT jbyteArray JNICALL Java_core_crypt_PBEPlatformNative_generate
  (JNIEnv *jenv, jclass jclazz, jstring jpassword, jbyteArray jsalt, jint jiterations, jint jkeyLengthBits)
{
	jboolean isCopy;
	const char *password = (*jenv)->GetStringUTFChars(jenv, jpassword, 0);
	jbyte *salt = (*jenv)->GetByteArrayElements (jenv, jsalt, &isCopy);
	jint saltLength = (*jenv)->GetArrayLength(jenv, jsalt);

	int keyLengthBytes = jkeyLengthBits/8;
	unsigned char *out = malloc(keyLengthBytes);

	const uint8_t *ucSalt = (const uint8_t *)salt;
	const uint8_t *ucPassword = (const uint8_t *)password;
	uint64_t u64Iterations = (uint64_t)jiterations;
	s_PBKDF2_SHA256 (password, strlen(password), ucSalt, saltLength, u64Iterations, out, keyLengthBytes);

	jbyteArray jout = (*jenv)->NewByteArray (jenv, keyLengthBytes);
	(*jenv)->SetByteArrayRegion (jenv, jout, 0, keyLengthBytes, out);
	free (out);

	(*jenv)->ReleaseByteArrayElements (jenv, jsalt, salt, isCopy);

	return jout;
}
