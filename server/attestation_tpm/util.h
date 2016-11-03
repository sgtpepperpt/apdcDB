#include <string>
#include <stdexcept>
#include <memory>
#include <string>
#include <vector>

#include <iostream>
#include <fstream>
#include <stdexcept>

using namespace std;

std::vector<std::string> split(const std::string &s, char delim);
std::string read_command(std::string comm);
std::vector<char> readFile(std::string n);
