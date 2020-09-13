/* Header for class VM */

#ifndef _Included_VM
#define _Included_VM
#include <jni.h>
#include <VM.h>
VM vm;
std::vector<Value>* mem = new std::vector<Value>();
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     VM
 * Method:    runWithString
 * Signature: (BLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_VM_runWithString
  (JNIEnv *, jobject, jbyte, jstring);

/*
 * Class:     VM
 * Method:    runWithDouble
 * Signature: (BLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_VM_runWithDouble
  (JNIEnv *, jobject, jbyte, jstring);

/*
 * Class:     VM
 * Method:    runWithBoolean
 * Signature: (BZ)V
 */
JNIEXPORT void JNICALL Java_VM_runWithBoolean
  (JNIEnv *, jobject, jbyte, jboolean);

/*
 * Class:     VM
 * Method:    disassembleWithString
 * Signature: (BLjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_VM_disassembleWithString
  (JNIEnv *, jobject, jbyte, jstring);

/*
 * Class:     VM
 * Method:    disassembleWithDouble
 * Signature: (BLjava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_VM_disassembleWithDouble
  (JNIEnv *, jobject, jbyte, jstring);

/*
 * Class:     VM
 * Method:    disassembleWithBoolean
 * Signature: (BZ)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_VM_disassembleWithBoolean
  (JNIEnv *, jobject, jbyte, jboolean);

/*
 * Class:     VM
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_VM_init
  (JNIEnv *, jobject);

/*
 * Class:     VM
 * Method:    run
 * Signature: (B)V
 */
JNIEXPORT void JNICALL Java_VM_run
  (JNIEnv *, jobject, jbyte);

JNIEXPORT jstring JNICALL Java_VM_pop
  (JNIEnv *, jobject);

#ifdef __cplusplus
}
#endif
#endif
