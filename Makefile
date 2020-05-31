classes=$(subst .java,, $(foreach java-source, $(wildcard *.java), $(java-source).class))
all: jar # INF native-image
jar: $(classes)
	echo Manifest-Version: 1.0 > manifest.txt
	echo Main-Class: Main >> manifest.txt
	jar cvfm output.jar manifest.txt *.class
	$(RM) manifest.txt
%.class: %.java
	javac $<
native-image:
	native-image 2>&1 >/dev/null && echo native-image installed! || (echo native-image not found && exit 1)
	native-image -jar output.jar --no-fallback -H:ReflectionConfigurationFiles=META-INF/native-image/reflect-config.json
INF:
	mkdir -p META-INF/native-image
	java -agentlib:native-image-agent=config-output-dir=META-INF/native-image Main samplecode || true
clean:
	$(RM) $(classes) output.jar output
