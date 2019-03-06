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
#include <sys/ioctl.h>
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

struct sockaddr_in my_socket;
int my_socket_fd = -1; 

struct sockaddr_in neighbor_socket;
int neighbor_socket_fd = -1; 

struct sockaddr_in receiver_socket;
char* messagge;
pthread_mutex_t messagge_mutex;

pthread_t token_handling_thread;
pthread_t terminal_thread;

void parse(int argc, char **argv);
void configure_my_socket();
void connect_neighbor_socket();
void *terminal(void* args);
void *token_handling(void* args);
void sigint_handler(int signo);
void clean_up();

int main (int argc, char **argv){

    if(signal(SIGINT,sigint_handler)==SIG_ERR) ERR("signal SIGINT error");
	if(atexit(clean_up)==-1) ERR("atexit error");

    parse(argc,argv);

    configure_my_socket();
    //connect_neighbor_socket();
    printf("Hi, I am client %s\n",my_name);

    sigset_t set;
    int sig;
    sigemptyset(&set);
    sigaddset(&set, SIGINT);
    pthread_sigmask(SIG_BLOCK, &set, NULL);
    
    if(pthread_create(&token_handling_thread,  NULL, token_handling, NULL)==-1) ERR("token handling pthread_create error");
    if(pthread_create(&terminal_thread,  NULL, terminal, NULL)==-1) ERR("terminal pthread_create error");
    
    sigwait(&set, &sig); 
    exit(EXIT_SUCCESS);
}

void parse(int argc, char **argv){
	if(argc != 6) MYERR("Incorrect number of arguments\n");
    my_name = argv[1];

    //prepare address structures
    struct in_addr addr; 
    memset(&addr, 0, sizeof(addr));
    if(inet_pton(AF_INET, argv[2], &addr) < 0) ERR("Inet_pton failed") //przekształca podany jako string adres 	

	memset(&neighbor_socket, 0, sizeof(neighbor_socket));
	neighbor_socket.sin_family = AF_INET;
	neighbor_socket.sin_port = htons((uint16_t) atoi(argv[3]));
	neighbor_socket.sin_addr = addr; 

    has_token = strcmp(argv[4],"token") == 0;

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

	memset(&my_socket, 0, sizeof(my_socket));
	my_socket.sin_family = AF_INET;
	my_socket.sin_port = 0; //next available high port.
	my_socket.sin_addr = addr; 

    if(bind(my_socket_fd,(struct sockaddr*) &my_socket, sizeof(struct sockaddr)) == -1) ERR("Bind inet failed")

    if (sock_type == SOCK_STREAM)
        if(listen(my_socket_fd, CLIENT_MAX) == -1) ERR("Listen inet failed")
}

void connect_neighbor_socket(){
    if ((neighbor_socket_fd = socket(AF_INET, sock_type, 0)) < 0) ERR("Socket inet failed")
    if (connect(neighbor_socket_fd,(struct sockaddr*) &neighbor_socket, sizeof(struct sockaddr_in)) < 0) ERR("Connect failed")
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

        char *context;
    
        char * ip_addr_str = strtok_r (input, delimiter, &context);
        if (ip_addr_str == NULL || ip_addr_str[0] == '\0'){
            printf("IP address is empty!\n");
            continue;
        }
        char * port_str = strtok_r (NULL, delimiter, &context);
        if (port_str == NULL || port_str[0] == '\0'){
            printf("Port number is empty!\n");
            continue;
        }
        
        if (context == NULL || context[0] == '\0'){
            printf("Messagge is empty!\n");
            continue;
        }

        pthread_mutex_lock(&messagge_mutex);

        //prepare address structures
        struct in_addr addr; 
        memset(&addr, 0, sizeof(addr));
        if(inet_pton(AF_INET, ip_addr_str, &addr) != 1){
            printf("IP address is not valid!\n");
            pthread_mutex_unlock(&messagge_mutex);
            continue;
        }

        memset(&receiver_socket, 0, sizeof(receiver_socket));
        receiver_socket.sin_family = AF_INET;
        receiver_socket.sin_port = htons((uint16_t) atoi(port_str));
        receiver_socket.sin_addr = addr; 

        messagge = context;
        pthread_mutex_unlock(&messagge_mutex);

        /*
        printf("IP address: %s\n", ip_addr_str);
        printf("Port: %s\n", port_str);
        printf("Messagge: %s\n", messagge);
        */
    }
   return (void *) 0;
}

bool is_my_socket(struct sockaddr_in receiver){
    //struct in_addr receiver_addr = (struct in_addr) receiver.sin_addr;
    //struct in_addr my_addr = (struct in_addr) my_socket.sin_addr;
    return ((receiver.sin_addr.s_addr == my_socket.sin_addr.s_addr) && (receiver.sin_family == my_socket.sin_family) && (receiver.sin_port == my_socket.sin_port));
}

void *token_handling(void* args){
	char buff[CONTENT_MAX];
    token_t token;
    int len;
    
    while(1){
        ioctl(my_socket_fd, FIONREAD, &len);
        if (len <= 0){ 
            continue;            
        }

        read(my_socket_fd, &token, sizeof(token_t));
        read(my_socket_fd, buff, token.msg_size);

        if(token.msg_size!=0){
            
            if(is_my_socket(token.receiver)){
                printf("Messagge: %s",buff);
            }else{
                pass_token(token,buff);
            }
        }else{
            pass_token(token,buff);
            //TODO send messagge
        }   
    }
    
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

    pthread_cancel(terminal_thread);
    pthread_cancel(token_handling_thread);
}