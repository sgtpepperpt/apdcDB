#include<stdio.h>
#include<string.h>
#include<stdlib.h>

#include <ctime>
#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <stdexcept>
#include <memory>

#include "main.h"
#include "util.h"
#include "base64.h"

using namespace std;

const int DEFAULT_LISTENER_PORT = 7868;

const string MYSQL_HASH_COMM	= "sha1sum /usr/bin/mysql";
const string CBIR_HASH_COMM	= "sha1sum /home/pepper/apdc/server/cbir/* | sha1sum";

int main(int argc,char **argv){
	TSS_HCONTEXT	hContext;
	TSS_HTPM	hTPM;
	TSS_RESULT	result;
	BYTE		wks[2];

	memset(wks, 0, 20);
/*
	system("/etc/init.d/mysql stop");	
	printf("MySQL exited!\n");
	*/
	
	result = Tspi_Context_Create(&hContext);
	DBG("Create Context", result);
	
	result = Tspi_Context_Connect(hContext, NULL);
	DBG("Context Connect", result);
	
	// Get the TPM handle
	result=Tspi_Context_GetTpmObject(hContext,&hTPM);
	DBG("Get TPM Handle", result);
	
	//chdir("/home/pepper/apdc/server/attestation_tpm");
	
	//if set, write pcr registers
	if(argc == 2 && strcmp(argv[1], "-w") == 0){
		//string mySQL_hash = split(read_command(MYSQL_HASH_COMM), ' ').at(0);
		string cbir_hash = split(read_command(CBIR_HASH_COMM), ' ').at(0);
	
		//read_PCR(hTPM, 10);
		read_PCR(hTPM, 11);
	
		//write_PCR(hTPM, 10, const_cast<char*>(mySQL_hash.c_str()));
		write_PCR(hTPM, 11, const_cast<char*>(cbir_hash.c_str()));
	}
	
	// MAIN BODY VARIABLES
	for(int i = 0; i < 19; i++)
		read_PCR(hTPM, i);
	
	//wait for requests and answer
	socks();
	
	// Clean up
	Tspi_Context_Close(hTPM);
	Tspi_Context_FreeMemory(hContext,NULL);
	Tspi_Context_Close(hContext);
	
	return 0;
}

// based on http://www.binarytides.com/server-client-example-c-sockets-linux/
int socks(){
    struct sockaddr_in server, client;

    //create socket
    int socket_desc = socket(AF_INET, SOCK_STREAM, 0);
    if (socket_desc == -1){
        perror("Could not create socket!");
        exit(1);
    }
    
    printf("Socket created!\n");
    
    //prepare the sockaddr_in structure
    server.sin_family = AF_INET;
    server.sin_addr.s_addr = INADDR_ANY;
    server.sin_port = htons(DEFAULT_LISTENER_PORT);
     
    //allow to reuse socket quickly after a previous instance
	int enable = 1;
	int reuse_sock = setsockopt(socket_desc, SOL_SOCKET, SO_REUSEADDR, &enable, sizeof(int));
	if (reuse_sock < 0) {
		perror("Set socket reuse failed!");
		exit(1);
	}  
     
    //bind socket
    int bind_sock = bind(socket_desc, (struct sockaddr*) &server, sizeof(server));
    if(bind_sock < 0){
        perror("Bind socket failed!");
        exit(1);
    }
    
    printf("Binding done!\n");
	
    //listen
    listen(socket_desc , 3);
    printf("Waiting for incoming connections...\n");
    
    int c = sizeof(struct sockaddr_in);

	while(1){
		//accept connection from an incoming client
		int client_sock = accept(socket_desc, (struct sockaddr *) &client, (socklen_t*) &c);
		if (client_sock < 0){
		   perror("Accept failed!");
		   exit(1);
		}
	
		printf("Connection accepted\n");
		 
		//Receive a message from the client
   		char buffer_s[10];
    	int receive, read_size = 0;
    	
    	printf("!\n");
    	receive = recv(client_sock, buffer_s, 10, 0);
    	if(receive != 10){
    		perror("Message formatting error!");
    		exit(1);
    	}
    	
    	int total_size = atoi(buffer_s);
    	printf("%d total \n", total_size);
    	
    	string request;
    	
    	//keep reading message until we have all bytes
    	while(read_size < total_size){
    		char tmp_buffer[128];
    		
    		int read = recv(client_sock, tmp_buffer, 128, 0);
    		if(read == -1){
        		perror("recv failed");
        		exit(1);
			}
    		
    		request.append(tmp_buffer);
    		
    		printf("%d read %s\n", read_size, request.c_str());
    		read_size += read;
    	}

    	//all of the message was read
    	printf("Read %d,%d,%d bytes, processing...\n", (int) strlen(request.c_str()), read_size, total_size);
		
    	//process message
    	std::vector<char> result;
    	if(request.find("quote") == 0){
    		result = readFile("data/tmp/pcrvals");
    		result.push_back('\n');
    		result.push_back('\n');
    		
    		std::vector<char> quote = processQuoteRequest(request);
    		for(int k = 0; k < quote.size(); k++)
    			result.push_back(quote.at(k));    		
    	}
    	
    	/*cout<<"%%"<<endl<<endl<<"%%"<<endl<<result.size()<<endl;
    	for(int as = 0; as<result.size(); as++)
    		cout << result.at(as);*/
    		
    	char * buffer = (char*) malloc(result.size() * sizeof(char));
    	for(int i = 0; i < result.size(); i++)
    		buffer[i] = result.at(i);
    	
    	//answer to client
    	char to_client[10 + result.size()];
    	sprintf(to_client, "%010zu%s", result.size(), buffer);
    	
    	write(client_sock, to_client, sizeof(to_client));
    	close(client_sock);
    	printf("size: %zu", sizeof(to_client));
    }
	close(socket_desc);
    return 0;
}

//http://stackoverflow.com/questions/22059189/read-a-file-as-byte-array
std::vector<char> readFile(string filename){
	ifstream ifs(filename, ios::binary|ios::ate);
    ifstream::pos_type pos = ifs.tellg();

    std::vector<char>  result(pos);

    ifs.seekg(0, ios::beg);
    ifs.read(&result[0], pos);

    return result;
}

std::vector<char> processQuoteRequest(string request){
	cout << "Processing quote request from client..." << endl;
	
	//parse request from client
	vector<string> arguments = split(request, '\n');
	string pcrvals = arguments.at(1);
	string nonce = arguments.at(2);
	nonce.resize(20); // nonce must be 20 bytes
	
	chdir("data/tmp"); //go to working dir
	
	//save nonce in file
	ofstream n_file ("nonce");
	if (n_file.is_open()){
		n_file << nonce;
		n_file.close();
	}
	else {
		perror("Error saving nonce file!");
		exit(1);
	}
	
	//start tpm interaction
    //try to load key, if needed
    string output = read_command("tpm_loadkey ../blob-aik ../uuid");
    if(output.find("TSS_E_KEY_ALREADY_REGISTERED") != string::npos){
    	printf("Key already on TPM, continuing...\n");
    }
    
    //generate quote
    output = read_command("tpm_getquote -p pcrvals ../uuid nonce quote " + pcrvals);
    if(output.length() > 0){
    	cout << "Something may have gone wrong generating a quote" << endl;
    	cout << output << endl;
    }
    
    //get quote file to return
	std::vector<char> quote = readFile("quote");
	
	/*cout<<"--"<<endl;
	for(int gg = 0; gg<quote.size();gg++)
		cout << quote.at(gg);
	cout<<endl<<"--";*/
	
	chdir("../..");
	
	return quote;
}

void read_PCR(TSS_HTPM hTPM, int pcr){
	BYTE		*rgbPcrValue;
	UINT32		ulPcrValueLength = 20;
	int			i;
	UINT32		j = (UINT32) pcr;
//	TSS_RESULT	result;
	
/*	result = */Tspi_TPM_PcrRead(hTPM, j, &ulPcrValueLength, &rgbPcrValue);
	printf("PCR %02d ", j);
		
	for(i = 0; i < 19; ++i){
		printf("%02x", *(rgbPcrValue+i));
	}
	
	printf("\n");
}

int write_PCR(TSS_HTPM hTPM, int pcrToExtend, char* value){
	printf("write_PCR on pcr %d, write \"%s\"...\n", pcrToExtend, value);
	
	TSS_RESULT	result;
	UINT32		PCR_result_length;
	BYTE		*Final_PCR_Value;
	BYTE		valueToExtend[250];

	memcpy(valueToExtend, value, strlen(value));
	
	//Extend the value
	result = Tspi_TPM_PcrExtend(hTPM, pcrToExtend, 20, (BYTE *) valueToExtend,
								NULL, &PCR_result_length, &Final_PCR_Value);
	
	DBG("Extended the PCR", result);
	return 0;
}
