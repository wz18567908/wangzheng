INCLUDE_DIR="__INCLUDE_DIR__"

CFLAGS=-I$(INCLUDE_DIR) -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

LSFBATCHLIB=libbat.so liblsf.so

LIB=$(LSFBATCHLIB) -lm -lnsl -ldl

LIBNAME=liblsb4j.so
OBJECTS=$(patsubst %.c,%.o,$(wildcard *.c))

.PHONY: clean all

all: $(LIBNAME)

%.o: %.c
	$(CC) -g -fpic -c $(CFLAGS) $< -o $@

$(LIBNAME) : $(OBJECTS)
	$(CC) -g -shared -o $@ $+ $(LIB)

clean:
	-rm -f liblsb4j.so
