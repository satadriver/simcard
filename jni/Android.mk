
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := cve20153636
LOCAL_SRC_FILES := cve20153636.c systemApp.c
#LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -DDEBUG -D__ARM__ -Wunused
LOCAL_MODULE_TAGS := optional
LOCAL_LDFLAGS += -static
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE    := cve20136282
LOCAL_SRC_FILES := cve20136282.c  systemApp.c
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_SRC_FILES := suMoveApp.c  systemApp.c
LOCAL_MODULE := suMoveApp
LOCAL_LDFLAGS   += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_SRC_FILES := cowroot.c  systemApp.c
LOCAL_MODULE := cowroot
LOCAL_LDFLAGS   += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE    := GoogleService
LOCAL_SRC_FILES := GoogleService.cpp  systemApp.c
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE    := uploadQQWXdb
LOCAL_SRC_FILES := uploadQQWXdb.cpp public.cpp
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE    := SU
LOCAL_SRC_FILES := SU.cpp
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE := GoogleServiceRoot
LOCAL_SRC_FILES := GoogleServiceRoot.cpp public.cpp
LOCAL_LDLIBS += -llog
include $(BUILD_SHARED_LIBRARY)


include $(CLEAR_VARS)
LOCAL_CPPFLAGS += -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_CFLAGS += -ffunction-sections -fdata-sections -fvisibility=hidden
LOCAL_LDFLAGS += -Wl,--gc-sections
LOCAL_MODULE    := run-as
LOCAL_SRC_FILES := run-as.cpp
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)


include $(CLEAR_VARS)
LOCAL_MODULE    := dirtycow
LOCAL_SRC_FILES := dirtycow.c
LOCAL_LDLIBS += -llog
LOCAL_CFLAGS += -pie -fPIE
LOCAL_LDFLAGS += -pie -fPIE
include $(BUILD_EXECUTABLE)



include $(CLEAR_VARS)
LOCAL_MODULE    := RunningApps
LOCAL_SRC_FILES := RunningApps.cpp
LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)
