#include<sys/stat.h>
#include<sys/types.h>
#include<sys/socket.h>
#include<arpa/inet.h>
#include<unistd.h>

//tpm related
#include<tss/tss_error.h>
#include<tss/platform.h>
#include<tss/tss_defines.h>
#include<tss/tss_typedef.h>
#include<tss/tss_structs.h>
#include<tss/tspi.h>
#include<trousers/trousers.h>

using namespace std;
/*
struct file_data{
	long size;
	const char * file;
};
*/
int socks();
std::vector<char> processQuoteRequest(std::string request);
std::vector<char> readFile(std::string n);

void read_PCR(TSS_HTPM hTPM, int pcr);
int write_PCR(TSS_HTPM hTPM, int pcrToExtend, char* value);

#define DBG(message, tResult) printf("(Line %d, %s) %s returned 0x%08x. %s.\n\n",__LINE__,__func__,message, tResult,(char *)Trspi_Error_String(tResult))
