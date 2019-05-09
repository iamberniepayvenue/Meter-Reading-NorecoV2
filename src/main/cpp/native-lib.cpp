//
// Created by Johnry Christian Paduhilao on 28/02/2019.
//

#include <jni.h>
#include "native-lib.h"

#include <string>

extern "C"
JNIEXPORT jstring JNICALL
Java_Utility_CallNative_getNative1(JNIEnv *env, jobject) {

    std::string secretkey = "MRBNORECO2";
    return env->NewStringUTF(secretkey.c_str());
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_Utility_CallNative_getNative2(JNIEnv *env,jobject) {
    std::string secretkey = "BLUKUA.CB1032020_GcETNV7wEH5C";
    return env->NewStringUTF(secretkey.c_str());
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_Utility_CallNative_getNative3(JNIEnv *env,jobject) {

    //std::string secretkey = "8080/noreco_api/billing_api.asp"; // dev
    std::string secretkey = "8080/teslaclient/noreco_api/billing_api.asp"; // prod
    return env->NewStringUTF(secretkey.c_str());
}

extern "C"
JNIEXPORT jstring
JNICALL
Java_Utility_CallNative_getNative4(JNIEnv *env,jobject) {
    std::string secretkey = "f5991507ef44d2b31c804d4a076089d2c54d3bb4";

    return env->NewStringUTF(secretkey.c_str());
}
