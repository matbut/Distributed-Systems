#ifndef HEADER_H
#define HEADER_H

#include <sys/socket.h>
#include <netinet/in.h>

#define QUEUE_MAX_SIZE 10
#define NAME_SIZE 7
#define UNKNOWN_NAME "unknow"
#define BUFFER_SIZE 1024
#define MESSAGE_SIZE 50
#define START_TTL 10

#define LOGGER_PORT 8888
#define LOGGER_IP_ADDR "224.1.1.1"

typedef enum message_t {
    CONNECT,
    SWITCH,
    DATA,
    EMPTY,
    UNKNOWN
} message_t;

#define TO_STRING(type) ( \
    type == CONNECT ? "CONNECT" : ( \
    type == SWITCH ? "SWITCH" : ( \
    type == DATA ? "DATA" : ( \
    type == EMPTY ? "EMPTY" : "UNKNOWN" \
    ))))

typedef struct token_t{
    uint8_t msg_ttl;
    char msg_dest[NAME_SIZE];
    char msg_from[NAME_SIZE];
    message_t msg_type;
    char msg[MESSAGE_SIZE];
} token_t;

typedef struct switch_t{
    char next_ip_addr[16];
    uint16_t next_port;
    char new_ip_addr[16];
    uint16_t new_port;
} switch_t;

/*
typedef struct token_t {
    message_t msg_type;
    int msg_size;
    int msg_num;
    char msg_receiver[MAX_NAME];
} token_t;

typedef struct queue_node_t {
    token_t token;
    char *message;
} queue_node_t;
*/


#endif

