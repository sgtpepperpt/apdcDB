#include <stdio.h>
#include <stdlib.h>

#include <string>
#include <sstream>
#include <vector>

#include "util.h"

using namespace std;

/* From http://ysonggit.github.io/coding/2014/12/16/split-a-string-using-c.html*/
vector<string> split(const string &s, char delim) {
    stringstream ss(s);
    string item;
    vector<string> tokens;
    while (getline(ss, item, delim)) {
        tokens.push_back(item);
    }
    return tokens;
}

/* Executes a bash command and returns both its stdout and stderr in a string */
string read_command(string comm){
	char buffer[128];
	comm.append(" 2>&1");
	
	string result = "";
	shared_ptr<FILE> pipe(popen(comm.c_str(), "r"), pclose);
	if (!pipe)
		throw runtime_error("popen() failed!");
	
	while (!feof(pipe.get())) {
		if (fgets(buffer, 128, pipe.get()) != NULL)
			result += buffer;
	}
	
	return result;
}
