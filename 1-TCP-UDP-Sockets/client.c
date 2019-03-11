#define _XOPEN_SOURCE 700
#define _DEFAULT_SOURCE

#include <netinet/in.h>
#include <pthread.h>
#include <signal.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>

#include "client.h"

#define ERR(str) {perror(str); exit(EXIT_FAILURE);}
#define MYERR(str) {printf(str); exit(EXIT_FAILURE);}

token_t token;
int queue_first = 0;
int queue_size = 0;
token_t* queue[QUEUE_MAX_SIZE];
pthread_mutex_t queue_mutex;

char my_name[NAME_SIZE];
uint16_t my_port;
char* neighbor_ip_addr;
uint16_t neighbor_port;
bool has_token;
uint16_t sock_type;

uint8_t my_pri = 0;
uint8_t token_id = 0;

int socket_fd;
struct sockaddr_in neighbor_addr;

int logger_socket_fd;
struct sockaddr_in logger_addr;

pthread_t token_handling_thread;
pthread_t terminal_thread;

void parse(int argc, char **argv);
int init_udp_socket(uint16_t port);
void init_neighbor_addr(char* neighbor_ip_addr,uint16_t neighbor_port);
void init_token_handling();
void *token_handling(void* args);
void *terminal(void* args);
void sigint_handler(int signo);
void clean_up();

void receive_token(struct sockaddr_in *receive_addr);
void send_token();
void send_old_token();
void send_new_token();
void send_connect_token();

void init_logger_socket();
void send_logger();

void queue_switch_message(switch_t* switch_ptr);
void queue_data_message(char receiver[], char *message);

void queue_add(token_t* token);
void queue_front_add(token_t* token);
token_t* queue_poll();
bool queue_is_empty();
bool queue_is_full();

int main(int argc, char **argv){

  if(signal(SIGINT,sigint_handler)==SIG_ERR) ERR("signal SIGINT error");
	if(atexit(clean_up)==-1) ERR("atexit error");
  
  parse(argc,argv);
  socket_fd = init_udp_socket(my_port);
  init_neighbor_addr(neighbor_ip_addr,neighbor_port);
  init_logger_socket();

  printf("Hi, I am %s\n",my_name);

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

	if(argc != 7) MYERR("Incorrect number of arguments\n");

  memcpy(my_name,argv[1],NAME_SIZE-1);
  my_name[NAME_SIZE-1]='\0';
  my_port = (uint16_t) atoi(argv[2]);
  neighbor_ip_addr = argv[3];
  neighbor_port = (uint16_t) atoi(argv[4]);
  has_token = strcmp(argv[5],"token") == 0;
  sock_type = (strcmp(argv[6],"TCP") == 0) ? SOCK_STREAM : SOCK_DGRAM;
}

int init_udp_socket(uint16_t port){

  struct sockaddr_in serverAddr;
  int socket_fd;

  if((socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) == -1) ERR("Socket inet failed");

  serverAddr.sin_family = AF_INET;
  serverAddr.sin_port = htons(port);
  serverAddr.sin_addr.s_addr = htonl(INADDR_ANY); //inet_addr("127.0.0.1");
  memset(serverAddr.sin_zero, '\0', sizeof serverAddr.sin_zero);  

  if(bind(socket_fd, (struct sockaddr *) &serverAddr, sizeof(serverAddr)) == -1) ERR("Bind failed");

  return socket_fd;
}

void init_neighbor_addr(char* neighbor_ip_addr,uint16_t neighbor_port){
    neighbor_addr.sin_family = AF_INET;
    neighbor_addr.sin_port = htons(neighbor_port);
    neighbor_addr.sin_addr.s_addr = inet_addr(neighbor_ip_addr);
    memset(neighbor_addr.sin_zero, '\0', sizeof neighbor_addr.sin_zero);  
}

void queue_front_add(token_t* token){
  pthread_mutex_lock(&queue_mutex);

  if(queue_size > QUEUE_MAX_SIZE)
    MYERR("Queue is full!")

  queue_first = (QUEUE_MAX_SIZE+queue_first-1)%QUEUE_MAX_SIZE;
  queue[queue_first] = token;
  queue_size++;

  pthread_mutex_unlock(&queue_mutex);
}

void queue_add(token_t* token){
  pthread_mutex_lock(&queue_mutex);

  if(queue_size > QUEUE_MAX_SIZE)
    MYERR("Queue is full!")

  queue[(queue_first+queue_size)%QUEUE_MAX_SIZE] = token;
  queue_size++;

  pthread_mutex_unlock(&queue_mutex);
}

token_t* queue_poll(){
  pthread_mutex_lock(&queue_mutex);
  token_t* token = queue[queue_first];

  queue_first = (queue_first+1)%QUEUE_MAX_SIZE;
  queue_size--;

  pthread_mutex_unlock(&queue_mutex);
  return token;
}

bool queue_is_empty(){
  pthread_mutex_lock(&queue_mutex);
  bool returned = queue_size == 0;
  pthread_mutex_unlock(&queue_mutex);
  return returned;
}

bool queue_is_full(){
  pthread_mutex_lock(&queue_mutex);
  bool returned = queue_size >= QUEUE_MAX_SIZE;
  pthread_mutex_unlock(&queue_mutex);
  return returned;
}

void *terminal(void* args){
	char* input = NULL;
  size_t inputLength = 0;

  char destination[NAME_SIZE];
  char messagge[MESSAGE_SIZE];
  while(1) {
    printf("Enter destination:");
    scanf("%6s",destination);
    destination[6] = '\0';

    while ((getchar()) != '\n');

    printf("Enter message:");
    scanf("%s", messagge);
    while ((getchar()) != '\n');

    if(queue_is_full()){
      printf("Please wait, message queue is full.\n");
    }else{
      printf("Enqueue messege to %s: %s\n",destination, messagge);
      queue_data_message(destination,messagge);
    }
    //if(getline(&input, &inputLength, stdin)==-1) ERR("getline error");
    //memcpy(token.msg ,input,NAME_SIZE);
  }
  return (void *) 0;
}

void send_connect_token(){
  char* my_ip_addr = "127.0.0.1"; //TODO get my ip addr
  switch_t* switch_ptr = malloc(sizeof(switch_t));
  memcpy(switch_ptr->new_ip_addr,my_ip_addr,strlen(neighbor_ip_addr)); 
  switch_ptr->new_port = my_port;
  memcpy(switch_ptr->next_ip_addr,neighbor_ip_addr,strlen(neighbor_ip_addr));
  switch_ptr->next_port = neighbor_port;

  token.id = 1;
  token.max_met_pri = 0;
  memcpy(token.msg,switch_ptr,sizeof(switch_t));
  memcpy(token.msg_dest,UNKNOWN_NAME,NAME_SIZE);
  memcpy(token.msg_from,my_name,NAME_SIZE);
  token.msg_ttl = 1;
  token.msg_type = CONNECT;

  send_token();
  free(switch_ptr);
}

void init_token_handling(){
  if(has_token){
    neighbor_ip_addr = "127.0.0.1"; //my loopback;
    neighbor_port = my_port;
    init_neighbor_addr(neighbor_ip_addr,neighbor_port);

    token.id = 0;
    token.max_met_pri = 0;
    memcpy(token.msg_dest,UNKNOWN_NAME,NAME_SIZE);
    memcpy(token.msg_from,my_name,NAME_SIZE);
    token.msg_ttl = 1;
    token.msg_type = EMPTY;

    send_token();
  }else{
    send_connect_token();
  }
}

void *token_handling(void* args){

  init_token_handling();

  struct sockaddr_in receive_addr;
  switch_t* switch_ptr = (switch_t*) &token.msg;

  while(1){
    receive_token(&receive_addr);
    
    switch (token.msg_type)
    {
      case CONNECT:
        queue_switch_message(switch_ptr);
        break;
      case SWITCH:
        if(  /* to me */
          (strcmp(switch_ptr->next_ip_addr,neighbor_ip_addr) == 0) && 
          switch_ptr->next_port == neighbor_port){ 
            neighbor_ip_addr = switch_ptr->new_ip_addr;
            neighbor_port = switch_ptr->new_port;
            //printf("change neighbor %s  %hhu\n",neighbor_ip_addr,neighbor_ip_addr);
            init_neighbor_addr(neighbor_ip_addr,neighbor_port);
            send_new_token();
          }else{
            send_old_token();
          }
        break;
      case DATA:
        if(strcmp(my_name,token.msg_dest) == 0){
          printf("\n!!!Received message: from %s: %s!!!\n",token.msg_from,token.msg);
          send_new_token();
        }else{  
          send_old_token();
        }
        break;        
      case EMPTY:
        send_new_token();
        break; 
      default:
        MYERR("Unknown message type");
    }    
  }
  return (void *) 0;
}

void send_old_token(){
  --token.msg_ttl;
  if(token.msg_ttl <= 0){
    send_new_token();
  }else{
    if(!queue_is_empty())
      if(my_pri<PRI_MAX) my_pri++;

    send_token(); 
  }
}

void send_new_token(){

  //token.id = 0; //keep old
  //token.max_met_pri = 0; //keep old
  memcpy(token.msg_dest,UNKNOWN_NAME,NAME_SIZE);
  memcpy(token.msg_from,my_name,NAME_SIZE);
  token.msg_ttl = 1;
  token.msg_type = EMPTY;

  uint8_t old_id = token.id;

  if(!queue_is_empty())
    if(token.max_met_pri >= my_pri+1){
      if(my_pri<PRI_MAX) 
        my_pri++;
    }else{
      token_t* token_ptr = queue_poll();
      memcpy(&token,token_ptr,sizeof(token_t));
      free(token_ptr);

      token.max_met_pri = my_pri = 0;
      token.id = old_id;
    }
  send_token();
}

void send_token(){
  char buffer[BUFFER_SIZE];
  size_t len = sizeof(token_t);

  if(token.max_met_pri < my_pri)
    token.max_met_pri = my_pri;

  if(token.msg_type!=CONNECT && token.id %2 == 0){
    token_id = ++token.id;
  }else if((token.msg_type!=CONNECT && token_id != 0)){
    token_id = 0;
    ++token.id;
  }
  
  memcpy(buffer, &token, len);
  sendto(socket_fd,buffer,len,0,(struct sockaddr *)&neighbor_addr,sizeof(neighbor_addr));
}

void receive_token(struct sockaddr_in *receive_addr){
  char buffer[BUFFER_SIZE];
  socklen_t addr_size;

  do{
    int len = recvfrom(socket_fd,buffer,BUFFER_SIZE,0,(struct sockaddr *)receive_addr, &addr_size);
    memcpy(&token, buffer, sizeof(token_t));;
    send_logger();
  }while((token.msg_type!=CONNECT) && (token_id != 0) && (token.id != token_id));
  
  sleep(1);
}

void queue_switch_message(switch_t* switch_ptr){
  token_t* token_ptr = malloc(sizeof(token_t));
  //token_ptr->id = 0; //keep old
  //token_ptr->max_met_pri = 0; //keep old
  memcpy(token_ptr->msg,switch_ptr,sizeof(switch_t));
  memcpy(token_ptr->msg_dest,UNKNOWN_NAME,NAME_SIZE);
  memcpy(token_ptr->msg_from,my_name,NAME_SIZE);
  token_ptr->msg_ttl = START_TTL;
  token_ptr->msg_type = SWITCH;

  queue_front_add(token_ptr);
  my_pri = PRI_SWITCH;
}

void queue_data_message(char receiver[], char *message){
  token_t* token_ptr = malloc(sizeof(token_t));
  memcpy(token_ptr->msg,message,strlen(message));
  memcpy(token_ptr->msg_dest,receiver,NAME_SIZE);
  memcpy(token_ptr->msg_from,my_name,NAME_SIZE);
  token_ptr->msg_ttl = START_TTL;
  token_ptr->msg_type = DATA;
  queue_add(token_ptr);
}

void sigint_handler(int signo){
	exit(EXIT_FAILURE);
}

void clean_up(){
    if(socket_fd != -1) 
        if (close(socket_fd))
            perror("Close failed");

    pthread_cancel(terminal_thread);
    pthread_cancel(token_handling_thread);
}

void init_logger_socket(){
  if ((logger_socket_fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0)
    ERR("Socket logger failed");

  logger_addr.sin_family = AF_INET;
  logger_addr.sin_port = htons(LOGGER_PORT);
  logger_addr.sin_addr.s_addr = inet_addr(LOGGER_IP_ADDR);
  memset(logger_addr.sin_zero, '\0', sizeof logger_addr.sin_zero); 
}

void send_logger(){
  char buffer[128];
  sprintf(buffer,"%6s pri %3hhu received token %7s from %6s to %6s with ttl %2hhu met pri %3hhu id %3hhu",my_name,my_pri,TO_STRING(token.msg_type),token.msg_from,token.msg_dest,token.msg_ttl,token.max_met_pri,token.id);
  if ((sendto(logger_socket_fd, buffer, strlen(buffer), 0, (struct sockaddr *) &logger_addr, sizeof(logger_addr))) < 0) 
    ERR("Socket logger failed");
}

//  printf("empty: %d full: %d first: %d size: %d\n",queue_is_empty(),queue_is_full(),queue_first,queue_size);



/*
void queue_switch_message(struct sockaddr_in* sender){
  queue_node_t queue_node;
  queue_node.token.msg_type = SWITCH;
  memcpy(queue_node.token.msg_receiver,my_name,MAX_NAME);
  queue_node.token.msg_size = sizeof(sender);
  queue_node.message = (char*) sender;
  queue_add(queue_node);
}

void send_token(token_t *token,char* message){

  char buffer[BUFFER_SIZE];

  token->msg_size = strlen(message);
  memcpy(buffer, token, sizeof(token_t));
  memcpy(buffer+sizeof(token_t), message, token->msg_size);

  int len = sizeof(token_t) + token->msg_size;

  sleep(1);
  sendto(socket_fd,buffer,len,0,(struct sockaddr *)&neighbor_addr,sizeof(struct sockaddr_in));
}

void receive_token(token_t *token,char* message,struct sockaddr_in *receive_addr){
  
  char buffer[BUFFER_SIZE];
  socklen_t addr_size;

  int len = recvfrom(socket_fd,buffer,BUFFER_SIZE,0,(struct sockaddr *)receive_addr, &addr_size);

  send_logger();

  memcpy(token, buffer, sizeof(token_t));
  memcpy(message, buffer+sizeof(token_t), token->msg_size);
}
*/