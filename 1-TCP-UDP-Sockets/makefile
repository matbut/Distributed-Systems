CC=gcc
CFLAGS=-Wall -std=c11 -ggdb 
LIBFLAGS=-lpthread
CLIENT=client
LOG=log.txt
SCRIPT=logger.py

$(CLIENT): $(CLIENT).c $(CLIENT).h
	$(CC) $(CFLAGS) $(CPPFLAGS) $(CLIENT).c -o $(CLIENT) $(LIBFLAGS)

.PHONY: clean

clean:
	-rm -rf *.o $(CLIENT) $(LOG) 

udp: $(CLIENT) $(SCRIPT)
	./client janusz 7893 127.0.0.1 7894 token UDP & 
	./client janina 7894 127.0.0.1 7893 notoken UDP & 
	./client waldek 7895 127.0.0.1 7894 notoken UD &
	./client bartek 7896 127.0.0.1 7894 notoken UDP &
	python3 $(SCRIPT) $(LOG) 


