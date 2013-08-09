LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := Mailiverse
LOCAL_SRC_FILES := \
	Mailiverse.cpp \
	core_crypt_PBEPlatformNative.c \
	sha256.c

include $(BUILD_SHARED_LIBRARY)
