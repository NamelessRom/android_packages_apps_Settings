LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_JAVA_LIBRARIES := bouncycastle conscrypt telephony-common telephony-msim
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4 android-support-v13 jsr305

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := \
        $(call all-java-files-under, src) \
        src/com/android/settings/EventLogTags.logtags

LOCAL_SRC_FILES += \
        src/com/android/display/IPPService.aidl

LOCAL_PACKAGE_NAME := Settings
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_AAPT_INCLUDE_ALL_RESOURCES := true
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

## Holocolorpicker

library_src_files := ../../../external/holocolorpicker/src
LOCAL_SRC_FILES   += $(call all-java-files-under, $(library_src_files))

LOCAL_RESOURCE_DIR += $(LOCAL_PATH)/../../../external/holocolorpicker/res

######

LOCAL_AAPT_FLAGS := \
    --auto-add-overlay \
    --extra-packages com.larswerkman.holocolorpicker \

######

LOCAL_JAVA_LIBRARIES += org.cyanogenmod.hardware

include $(BUILD_PACKAGE)

# Use the folloing include to make our test apk.
ifdef BUILD_TEST_APPS
include $(call all-makefiles-under,$(LOCAL_PATH))
endif
