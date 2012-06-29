#include <stdio.h>
#include <netdb.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>
#include <pthread.h>


#define MAX_FILE_SIZE 300000
#define MAX_MSG_SIZE 1500
#define MAX_BLOB_SIZE 1400
#define MAX_SEND_ATTEMPTS 100

//#define DEBUG

typedef struct _oscMessage
{
    int size;
    char *ptr;
    char *data;
} * oscMessage;

typedef struct _oscBlob
{
    int size_to_send;
    int padded_size;
    char *ptr;
    char *data;
} * oscBlob;

void waitForAck(void);
char * readString(char *buf);
int receivedAck(int offset);
int swap(int i);
int computeCRC(char *data, int size);
void resetMessage(oscMessage msg);
void pad(oscBlob b);
void addToMessageUnpadded(oscMessage msg, char* src, int size);
void addStringToMessage(oscMessage msg, char* src);
void addFloatToMessage(oscMessage msg, float f);
void addIntToMessage(oscMessage msg, int i);

void *receiveTask(void *param);

int lastAck = -1;
char runThread;

struct sockaddr_in my_addr, lem_addr;

void *receiveTask(void *param)
{
    char buf[MAX_MSG_SIZE];
    char *b;
    int length, received, sock;
    struct sockaddr_in their_addr;

        sock = *((int *) param);

        length = sizeof(their_addr);


    while(runThread)
    {
        received = recvfrom(sock, buf, MAX_MSG_SIZE, 0, (struct sockaddr *) &their_addr, (socklen_t *) &length);
        if(received <= 0 || (htonl(their_addr.sin_addr.s_addr) != htonl(lem_addr.sin_addr.s_addr))) continue;
        if (strcmp(buf, "/interface") != 0) continue;
        /* skip msg name and type tags*/
        b = readString(buf);
        b = readString(b);

        /* skip zero size argument */
        b += 4;

        lastAck = ntohl(*((long *)b));
        #ifdef DEBUG
        printf("Received Ack for offset %i\n", lastAck);
        #endif
    }

    printf("Ending\n");

}

void waitForAck(void)
{
    usleep(10000); // sleep 10ms
}

char * readString(char *buf)
{
    unsigned long *reader;
    unsigned long mask = 0xFF;
    reader = (unsigned long *) buf;
    while((*reader) & mask)
    {
    reader++;
    }
    reader++;
    return (char *) reader;
}

int receivedAck(int offset)
{
    return (lastAck == offset);
}

/* this function is necessary on big-endian architectures */
int swap(int i)
{
#ifdef __BIG_ENDIAN__
    return (((i & 0xFF)<<24) | ((i & 0xFF00)<<8) | ((i>>8) & 0xFF00) | ((i>>24) & 0xFF));
#else
        return i;
#endif
}

int computeCRC(char *data, int size)
{
    int *ptr;
    int crc = 0;
    int i = 0;
    ptr = (int *)data;
    while(i != size)
    {
        crc += (swap(*ptr));
        i += 4;
        ptr++;
    }
    return crc;
}

void resetMessage(oscMessage msg)
{
    msg->size = 0;
    msg->ptr = msg->data;
}

void pad(oscBlob b)
{
    int i;

    i = (b->padded_size - b->size_to_send);
    while (i--)
    {
        *(b->ptr) = '\0';
        (b->ptr)++;
    }

}

void addToMessageUnpadded(oscMessage msg, char* src, int size)
{

    int i;
    for (i=size; i>0; i--)
    {
        *(msg->ptr) = *src;
        src++;
        (msg->ptr)++;

    }
    msg->size += size;
}

void addStringToMessage(oscMessage msg, char* src)
{
    int len;
    len = strlen(src) + 1;
    strncpy(msg->ptr, src, len + ((4 - (len&3))&3));
    msg->ptr += len + ((4 - (len&3))&3);
    msg->size += len + ((4 - (len&3))&3);
}

void addFloatToMessage(oscMessage msg, float f)
{
    *((float *) msg->ptr) = f;
    msg->ptr += 4;
    msg->size += 4;
}

void addIntToMessage(oscMessage msg, int i)
{
    *((int *) msg->ptr) = i;
    msg->ptr += 4;
    msg->size += 4;
}

int main(int argc, char** argv)
{
    FILE *pFile;
    oscMessage msg;
    int offset, s, flags;
    char c;
    oscBlob b;
    int remaining_attempts;
    pthread_t thread;

    if (argc!=4 || (strcmp(argv[1], "-h") != 0))
    {
    printf("LemurLoader, version 0.1\n");
    printf("usage : LemurLoader -h lemur_ip file_path\n");
    return 0;
    }

    pFile = fopen(argv[3], "r");

    if(!pFile)
    {
        printf("Couldn't open file : %s\n", argv[3]);
        return 0;
    }

    if(!inet_addr(argv[2]))
    {
        printf("Couldn't resolve host : %s\n", argv[2]);
        return 0;
    }

    s = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);

    lem_addr.sin_family = AF_INET;
    lem_addr.sin_port = htons(8000);
    lem_addr.sin_addr.s_addr = inet_addr(argv[2]);

    my_addr.sin_family = AF_INET;
    my_addr.sin_addr.s_addr = htonl(INADDR_ANY);
    my_addr.sin_port = 0;

    /* set the socket to non-blocking mode */
    flags = fcntl(s, F_GETFL);
    fcntl(s, F_SETFL, flags | O_NONBLOCK);

    /* bind for ACK messages receiving */
    if(bind(s, (struct sockaddr *) &my_addr, sizeof(struct sockaddr))== -1)
    {
        printf("Error binding socket to local port\n");
        return 0;
    }

    /* init OSC msg */
    msg = (oscMessage) malloc(sizeof(struct _oscMessage));
    msg->data = (char *) malloc(MAX_MSG_SIZE);

    b = (oscBlob) malloc(sizeof(struct _oscBlob));

    b->size_to_send = 0;
    b->data = (char *) malloc(MAX_FILE_SIZE);
    b->ptr = b->data;

    b->ptr = stpcpy(b->ptr, "<RESET/>\n");

    do
    {
        c = (char) fgetc(pFile);
        *(b->ptr) = c;
        (b->ptr)++;
        (b->size_to_send)++;
    }
    while(c != EOF);
    *((b->ptr) - 1) = '\0';

    b->padded_size = b->size_to_send + ((4 - ((b->size_to_send)&3))&3);

    /* b->ptr should point to empty char after '\0' before calling pad(b) */
    pad(b);

    b->size_to_send = b->padded_size;

    b->ptr = b->data;

    offset = 0;

    runThread = 1;
    pthread_create(&thread, NULL, receiveTask, (void *) &s);

    while(b->size_to_send - MAX_BLOB_SIZE > 0)
    {

        b->size_to_send-=1400;

        resetMessage(msg);

        addStringToMessage(msg, "/interface");

        addStringToMessage(msg, ",iiib");

        addIntToMessage(msg, b->padded_size);

        addIntToMessage(msg, offset);

        addIntToMessage(msg, computeCRC(b->data, MAX_BLOB_SIZE));

        addIntToMessage(msg, 1400);

        addToMessageUnpadded(msg, b->data, MAX_BLOB_SIZE);

                remaining_attempts = MAX_SEND_ATTEMPTS;
                do
                {
                        if(remaining_attempts == 0)
                        {
                                printf("Send failed : Max send attempts reached\n");
                                return 0;
                        }
                        sendto(s, msg->data, msg->size, 0, (struct sockaddr *)&lem_addr, sizeof(struct sockaddr));
                        remaining_attempts--;
                        waitForAck();
        }
                while(!receivedAck(offset));
                #ifdef DEBUG
                printf("Send attempts number : %i\n", MAX_SEND_ATTEMPTS - remaining_attempts);
                #endif
        offset += MAX_BLOB_SIZE;

        b->data += MAX_BLOB_SIZE;
    }

    resetMessage(msg);

    addStringToMessage(msg, "/interface");

    addStringToMessage(msg, ",iiib");

    addIntToMessage(msg, b->padded_size);

    addIntToMessage(msg, offset);

    addIntToMessage(msg, computeCRC(b->data, b->size_to_send));

    addIntToMessage(msg, b->size_to_send);

    addToMessageUnpadded(msg, b->data, b->size_to_send);

        remaining_attempts = MAX_SEND_ATTEMPTS;
        do
        {
                if(remaining_attempts == 0)
                {
                        printf("Send failed : Max send attempts reached\n");
                        return 0;
                }
                sendto(s, msg->data, msg->size, 0, (struct sockaddr *)&lem_addr, sizeof(struct sockaddr));
                remaining_attempts--;
                waitForAck();
        }
        while(!receivedAck(offset));
        runThread = 0;
        pthread_join(thread, NULL);
        #ifdef DEBUG
        printf("Send attempts number : %i\n", MAX_SEND_ATTEMPTS - remaining_attempts);
        #endif

    printf("Send successful\n");

    close(s);
}
