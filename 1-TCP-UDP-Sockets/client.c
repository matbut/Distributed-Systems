#define _XOPEN_SOURCE 700
#define _DEFAULT_SOURCE

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include <signal.h>
#include <sys/wait.h>
#include <math.h>
#include <errno.h>
#include <unistd.h>
#include <sys/times.h>
#include <time.h>
#include <pthread.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <endian.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <stdbool.h>
#include "header.h"

#define ERR(str) {perror(str); exit(EXIT_FAILURE);}
#define MYERR(str) {printf(str); exit(EXIT_FAILURE);}

int sock_type;
bool has_token = false;

char* my_name = NULL;
struct sockaddr* my_socket = NULL;
int my_socket_fd = -1; 

struct sockaddr* neighbor_socket = NULL;
int neighbor_socket_fd = -1; 

void parse(int argc, char **argv);
void configure_my_socket();
void connect_neighbor_socket();
void *terminal(void* args);
void sigint_handler(int signo);
void clean_up();

int main (int argc, char **argv){

    if(signal(SIGINT,sigint_handler)==SIG_ERR) ERR("signal SIGINT error");
	if(atexit(clean_up)==-1) ERR("atexit error");

    parse(argc,argv);

    configure_my_socket();
    //connect_neighbor_socket();
    printf("Hi, I am client %s\n",my_name);

    terminal(NULL);

    //my_routine();
    
    exit(EXIT_SUCCESS);
}

void parse(int argc, char **argv){
	if(argc != 6) MYERR("Incorrect number of arguments\n");
    my_name = argv[1];

    //prepare address structures
    struct in_addr addr; 
    memset(&addr, 0, sizeof(addr));
    if(inet_pton(AF_INET, argv[2], &addr) < 0) ERR("Inet_pton failed") //przekształca podany jako string adres 	

	struct sockaddr_in sin_addr;
	memset(&sin_addr, 0, sizeof(sin_addr));
	sin_addr.sin_family = AF_INET;
	sin_addr.sin_port = htons(atoi(argv[3]));
	sin_addr.sin_addr = addr; 

    my_socket = (struct sockaddr *) &sin_addr;
	
	neighbor_socket = (struct sockaddr*) &sin_addr;

    if(strcmp(argv[4],"token") == 0)
        has_token = true;
    else
        has_token = false;
    
    if(strcmp(argv[5],"TCP") == 0){
        sock_type = SOCK_STREAM;
    }else if(strcmp(argv[5],"UDP") == 0){
        sock_type = SOCK_DGRAM;
    }else MYERR("Not supported protocol\n");
}

void configure_my_socket(){
	//create socket
	if ((my_socket_fd = socket(AF_INET,sock_type,0)) == -1) ERR("Socket inet failed")

	int flag=1;
	if (setsockopt(my_socket_fd, SOL_SOCKET, SO_REUSEADDR, &flag, sizeof(int)) < 0) ERR("Setsockopt failed")
	
	//prepare address structures
    struct in_addr addr; 
    memset(&addr, 0, sizeof(addr));
    addr.s_addr = INADDR_ANY; //any ip address

	struct sockaddr_in sin_addr;
	memset(&sin_addr, 0, sizeof(sin_addr));
	sin_addr.sin_family = AF_INET;
	sin_addr.sin_port = 0; //next available high port.
	sin_addr.sin_addr = addr; 

    my_socket = (struct sockaddr *) &sin_addr;
	
    if(bind(my_socket_fd,my_socket, sizeof(struct sockaddr)) == -1) ERR("Bind inet failed")

    if (sock_type == SOCK_STREAM)
        if(listen(my_socket_fd, CLIENT_MAX) == -1) ERR("Listen inet failed")
}

void connect_neighbor_socket(){
    if ((neighbor_socket_fd = socket(AF_INET, sock_type, 0)) < 0) ERR("Socket inet failed")
    if (connect(neighbor_socket_fd,neighbor_socket, sizeof(struct sockaddr_in)) < 0) ERR("Connect failed")
}

void pass_token(token_t token, char* messagge){
    sleep(1);
    void *buff;

    buff = malloc(sizeof(token_t) + token.msg_size);
    memcpy(buff, &token, sizeof(token_t));
    memcpy(buff+sizeof(token_t), messagge, token.msg_size);
    
    if (write(neighbor_socket_fd, buff, sizeof(token_t) + token.msg_size) < 0) 
        MYERR("write error");
}


void *terminal(void* args){
	char* input = NULL;
    size_t inputLength = 0;
    char delimiter[] = " ";

    while(1) {
        if(getline(&input, &inputLength, stdin)==-1) ERR("getline error");
        inputLength = strlen(input);
        input[inputLength-1]='\0';  

        printf("%ld\n",inputLength);

        char *ip_addr_str = NULL, *port_str = NULL,*messagge = NULL,*context;
    
        ip_addr_str = strtok_r (input, delimiter, &context);
        if (ip_addr_str == NULL || ip_addr_str[0] == '\0'){
            printf("IP address is empty!\n");
            continue;
        }
        port_str = strtok_r (NULL, delimiter, &context);
        if (port_str == NULL || port_str[0] == '\0'){
            printf("Port number is empty!\n");
            continue;
        }
        messagge = context;
        if (messagge == NULL || messagge[0] == '\0'){
            printf("Messagge is empty!\n");
            continue;
        }

        printf("IP address: %s\n", ip_addr_str);
        printf("Port: %s\n", port_str);
        printf("Messagge: %s\n", messagge);

        //prepare address structures
        /*
        struct in_addr addr; 
        memset(&addr, 0, sizeof(addr));
        if(inet_pton(AF_INET, ip_addr_str, &addr) < 0){
            printf("IP address is not valid!\n");
            break;
        }

        struct sockaddr_in sin_addr;
        memset(&sin_addr, 0, sizeof(sin_addr));
        sin_addr.sin_family = AF_INET;
        sin_addr.sin_port = htons(atoi(port_str));
        sin_addr.sin_addr = addr; 
        */

        
    }
   return (void *) 0;
}

void *token_handling(){
	char messagge[CONTENT_MAX];
    token_t token;

    /* TODO
    while(1){
        read(my_socket_fd, &token, sizeof(token_t));

        if(token.msg_size!=0){
            read(my_socket_fd, messagge, token.msg_size);
            if(0==0){
                //READ MESSAGGE
            }else{
                pass_token(token,messagge);
            }
        }else{
            if(0==0){//propablity

                token.msg_num++;
                token.msg_size
        
                
                pass_token(token,messagge);
            }else{
                pass_token(token,messagge);
            }
        }   
    }
    */
   return (void *) 0;
}


void sigint_handler(int signo){
	exit(EXIT_FAILURE);
}

void clean_up(){
	if(sock_type == SOCK_STREAM && my_socket_fd != -1){
		if (shutdown(my_socket_fd, SHUT_RDWR)!=0)
            perror("Shutdown failed");
	}
    if(my_socket_fd != -1) 
        if (close(my_socket_fd))
            perror("Close failed");
}