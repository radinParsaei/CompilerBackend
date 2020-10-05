classes=$(subst .java,, $(foreach java-source, $(wildcard *.java), $(java-source).class))
INCLUDES:=-I. -IVM -IVM/Value -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin -I$(JAVA_HOME)/include/linux -I$(JAVA_HOME)/include/win32 -I/usr/lib/jvm/default-java/include -I/usr/lib/jvm/default-java/include/linux
override LDFLAGS:=$(LDFLAGS) -shared -fPIC

ifeq ($(USE_GMP_LIB),1)
override LDFLAGS:=$(LDFLAGS) -lgmp -lgmpxx
override CFLAGS:=$(CFLAGS) -DUSE_GMP_LIB
else
override INCLUDES:=$(INCLUDES) -IVM/BigNumber/src/BigNumber
objs=VM/number.o VM/BigNumber.o
rmobjs=rmobjs
endif

override CFLAGS:=$(CFLAGS) $(INCLUDES)

ifeq ($(OS),Windows_NT)
	EXT:=dll
	NAME:=VM
else
		NAME:=libVM
    UNAME := $(shell uname -s)
    ifeq ($(UNAME),Linux)
			EXT:=so
    endif
    ifeq ($(UNAME),Darwin)
			EXT=dylib
    endif
endif

all: $(rmobjs) $(objs) VM_JNI.o $(NAME).$(EXT) output.jar # native-image
.PHONY: all

rmobjs:
	$(RM) $(objs)

%.o:
	$(MAKE) -C VM -f Makefile.old $(subst VM/,,$@) EXT_CFLAGS="-fPIC $(EXT_CFLAGS)" EXT_LDFLAGS="$(EXT_LDFLAGS)"

VM_JNI.o: VM_JNI.cpp VM_JNI.h
	$(CXX) -c $(CFLAGS) "$(EXT_CFLAGS)" VM_JNI.cpp -fPIC "$(EXT_LDFLAGS)"

$(NAME).$(EXT): VM_JNI.o
	$(CXX) $(CFLAGS) "$(EXT_CFLAGS)" $(objs) VM_JNI.o VM/VM.cpp $(LDFLAGS) -o $(NAME).$(EXT) "$(EXT_LDFLAGS)"

output.jar: $(classes)
	echo Manifest-Version: 1.0 > manifest.txt
	echo Main-Class: Main >> manifest.txt
	jar cvfm output.jar manifest.txt *.class
	$(RM) manifest.txt
%.class: %.java
	javac $<
native-image: INF
	native-image 2>&1 >/dev/null && echo native-image installed! || (echo native-image not found && exit 1)
	native-image -jar output.jar --no-fallback -H:ReflectionConfigurationFiles=META-INF/native-image/reflect-config.json
INF: output.jar
	mkdir -p META-INF/native-image
	-java -agentlib:native-image-agent=config-output-dir=META-INF/native-image Main
clean:
	$(RM) *.class output.jar output *.o $(NAME).$(EXT)
