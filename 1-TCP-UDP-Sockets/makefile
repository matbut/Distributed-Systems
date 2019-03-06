CC=gcc
CFLAGS=-Wall -std=c11 -ggdb 
LIBFLAGS=-lpthread
CLIENT=client

$(CLIENT): $(CLIENT).c 
	$(CC) $(CFLAGS) $(CPPFLAGS) $^ -o $@ $(LIBFLAGS)

.PHONY: clean

clean:
	-rm -rf *.o $(CLIENT)

tcp: $(CLIENT)
	./$(CLIENT) janusz 192.168.0.61 8084 token TCP

	
