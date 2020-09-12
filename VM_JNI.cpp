#include <VM_JNI.h>

JNIEXPORT void JNICALL Java_VM_run (JNIEnv *, jobject, jbyte opcode) {
  vm.run1((int)opcode);
}

JNIEXPORT void JNICALL Java_VM_runWithString (JNIEnv* env, jobject, jbyte opcode, jstring str) {
  if (str == NULL) {
    vm.run1((char)opcode, null);
  } else {
    vm.run1((char)opcode, env->GetStringUTFChars(str, new jboolean(1)));
  }
}

JNIEXPORT void JNICALL Java_VM_runWithBoolean (JNIEnv *, jobject, jbyte opcode, jboolean boolean) {
  vm.run1((char)opcode, (bool) boolean);
}

JNIEXPORT void JNICALL Java_VM_runWithDouble (JNIEnv *env, jobject, jbyte opcode, jstring data) {
  if (data == NULL) {
    vm.run1((char)opcode, null);
  } else {
    vm.run1((char)opcode, NUMBER(env->GetStringUTFChars(data, new jboolean(1))));
  }
}

JNIEXPORT void JNICALL Java_VM_init (JNIEnv *, jobject) {
  vm.attachMem(mem);
}


JNIEXPORT jstring JNICALL Java_VM_disassemble (JNIEnv *env, jobject, jbyte opcode) {
  return env->NewStringUTF(VM::disassemble((int)opcode, null).toString().c_str());
}

JNIEXPORT jstring JNICALL Java_VM_disassembleWithString (JNIEnv* env, jobject, jbyte opcode, jstring str) {
  if (str == NULL) {
    return env->NewStringUTF(VM::disassemble((char)opcode, null).toString().c_str());
  } else {
    return env->NewStringUTF(VM::disassemble((char)opcode, env->GetStringUTFChars(str, new jboolean(1))).toString().c_str());
  }
}

JNIEXPORT jstring JNICALL Java_VM_disassembleWithBoolean (JNIEnv *env, jobject, jbyte opcode, jboolean boolean) {
  return env->NewStringUTF(VM::disassemble((char)opcode, (bool) boolean).toString().c_str());
}

JNIEXPORT jstring JNICALL Java_VM_disassembleWithDouble (JNIEnv *env, jobject, jbyte opcode, jstring data) {
  if (data == NULL) {
    return env->NewStringUTF(VM::disassemble((char)opcode, null).toString().c_str());
  } else {
    return env->NewStringUTF(VM::disassemble((char)opcode, NUMBER(env->GetStringUTFChars(data, new jboolean(1)))).toString().c_str());
  }
}
