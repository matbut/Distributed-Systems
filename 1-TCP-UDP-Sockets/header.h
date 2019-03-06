#ifndef HEADER_H
#define HEADER_H

#include <sys/socket.h>

#define CLIENT_MAX 10
#define CONTENT_MAX 64

typedef struct token_t {
    int msg_size;
    int msg_num;
    struct sockaddr_in receiver;
} token_t;

#endif

