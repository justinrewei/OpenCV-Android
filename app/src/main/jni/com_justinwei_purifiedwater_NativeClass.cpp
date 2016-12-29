#include "com_justinwei_purifiedwater_NativeClass.h"



JNIEXPORT jstring JNICALL Java_com_justinwei_purifiedwater_NativeClass_getMessageFromJNI
  (JNIEnv *env, jclass obj){
    return env->NewStringUTF("This is message from JNI");
}
