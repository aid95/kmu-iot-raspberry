############################################
# This is a project standard makefile..
############################################
JAVAC = javac
JAVA = java

###########################################
# IOTMAKERS_SDK_HOME
###########################################
IOTMAKERS_SDK_HOME = .
IOTMAKERS_SDK_LIBNAME = $(IOTMAKERS_SDK_HOME)/dist/JAVA_TCP_SDK_2.0.1.jar
THIRDPARTY_LIB_PATH = $(IOTMAKERS_SDK_HOME)/lib

###########################################
# FLAGs
###########################################
OUTDIR = .
JFLAGS = -g -Xlint
JDPATH = -d $(OUTDIR)

JCPATH = -classpath $(OUTDIR):$(IOTMAKERS_SDK_LIBNAME):$(THIRDPARTY_LIB_PATH)/*

###########################################
# Compile
###########################################
.SUFFIXES: .java .class

.java.class:
	$(JAVAC) $(JFLAGS) $(JCPATH) $(JDPATH) $*.java

###########################################
# SOURCE TREE
###########################################
JAVA_SOURCE = \
	./raspberry.java \

###########################################
# BUILD
###########################################
default: classes
classes: $(JAVA_SOURCE:.java=.class)

###########################################
# Util
###########################################
clean:
	$(RM) *.class

run:
	$(JAVA) $(JCPATH) raspberry
