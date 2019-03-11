CC=gcc
CFLAGS=-Wall -std=c11 -ggdb 
LIBFLAGS=-lpthread
CLIENT=client

$(CLIENT): $(CLIENT).c $(CLIENT).h
	$(CC) $(CFLAGS) $(CPPFLAGS) $(CLIENT).c -o $(CLIENT) $(LIBFLAGS)

.PHONY: clean

clean:
	-rm -rf *.o $(CLIENT)

udp: $(CLIENT)
	./client janusz 7893 127.0.0.1 7894 token UDP

