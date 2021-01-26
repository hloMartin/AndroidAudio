LOCAL_PATH := $(call my-dir)

include $(clear_VARS)

include $(LOCAL_PATH)/celt_sources.mk   #加载celt 所有.c的 mk
include $(LOCAL_PATH)/silk_sources.mk  #加载silk 所有.c 的mk
include $(LOCAL_PATH)/opus_sources.mk #加载opus 所有.c 的mk

SILK_SOURCES += $(SILK_SOURCES_FIXED)
CELT_SOURCES += $(CELT_SOURCES_ARM)
SILK_SOURCES += $(SILK_SOURCES_ARM)

LOCAL_PRELINK_MODULE := false

LOCAL_MODULE 	:= opustool
LOCAL_MODULE_FILENAME:=libopustool
LOCAL_CFLAGS 	:= -w -std=c11 -Os -DNULL=0 -DSOCKLEN_T=socklen_t -DLOCALE_NOT_USED -D_LARGEFILE_SOURCE=1 -D_FILE_OFFSET_BITS=64
LOCAL_CFLAGS 	+= -Drestrict='' -D__EMX__ -DOPUS_BUILD -DFIXED_POINT -DUSE_ALLOCA -DHAVE_LRINT -DHAVE_LRINTF -fno-math-errno
LOCAL_CFLAGS 	+= -DANDROID_NDK -DDISABLE_IMPORTGL -fno-strict-aliasing -fprefetch-loop-arrays -DAVOID_TABLES -DANDROID_TILE_BASED_DECODE -DANDROID_ARMV6_IDCT -ffast-math -D__STDC_CONSTANT_MACROS
LOCAL_CPPFLAGS 	:= -DBSD=1 -ffast-math -Os -funroll-loops -std=c++11
LOCAL_LDLIBS    := -lm -llog

LOCAL_SRC_FILES     := $(CELT_SOURCES) $(SILK_SOURCES) $(OPUS_SOURCES) $(OPUS_SOURCES_FLOAT)

LOCAL_SRC_FILES     += \
    ./opus/ogg/bitwise.c \
    ./opus/ogg/framing.c

LOCAL_SRC_FILES     += \
	./opus/opusfile/http.c \
    ./opus/opusfile/info.c \
    ./opus/opusfile/internal.c \
    ./opus/opusfile/opusfile.c \
    ./opus/opusfile/wincerts.c \
    ./opus/opusfile/stream.c


LOCAL_SRC_FILES     += \
    ./audio.c \
	./utils.c

LOCAL_C_INCLUDES    := \
    $(LOCAL_PATH)/opus/include \
    $(LOCAL_PATH)/opus/src \
    $(LOCAL_PATH)/opus/silk \
    $(LOCAL_PATH)/opus/silk/fixed \
    $(LOCAL_PATH)/opus/celt \
    $(LOCAL_PATH)/opus/ogg \
    $(LOCAL_PATH)/opus/opusfile \



include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)
