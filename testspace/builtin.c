#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>

char* __mx_builtin_malloc(int n) {
    return (char*) malloc(n);
}

int __mx_builtin_arr_size(int* arr) {
    return arr[-1];
}

int __mx_builtin_str_len(char* str) {
    return strlen(str);
}
char* __mx_builtin_str_sub(char* str, int l, int r) {
    char* ret = (char*) malloc(strlen(str) + 1);
    strcpy(ret, str + l);
    ret[r - l] = '\0';
    return ret;
}
int __mx_builtin_str_parse(char* str) {
    int ret;
    sscanf(str, "%d", &ret);
    return ret;
}
int __mx_builtin_str_ord(char* str, int pos) {
    return str[pos];
}

char* __mx_builtin_str_cat(char* lhs, char* rhs) {
    char* ret = (char*) malloc(strlen(lhs) + strlen(rhs) + 1);
    strcpy(ret, lhs);
    return strcat(ret, rhs);
}
bool __mx_builtin_str_eq(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) == 0;
}
bool __mx_builtin_str_ne(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) != 0;
}
bool __mx_builtin_str_lt(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) < 0;
}
bool __mx_builtin_str_le(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) <= 0;
}
bool __mx_builtin_str_gt(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) > 0;
}
bool __mx_builtin_str_ge(char* lhs, char* rhs) {
    return strcmp(lhs, rhs) >= 0;
}

void __mx_builtin_printInt(int n) {
    printf("%d", n);
}
void __mx_builtin_printlnInt(int n) {
    printf("%d\n", n);
}
void __mx_builtin_print(char* str) {
    printf("%s", str);
}
void __mx_builtin_println(char* str) {
    printf("%s\n", str);
}
int __mx_builtin_getInt() {
    int ret; char ch;
    scanf("%d%c", &ret, &ch);
    return ret;
}
char* __mx_builtin_getString() {
    char* buff = (char*) malloc(1 << 15);
    int idx = 0;
    char ch;
    while(1) {
        scanf("%c", &ch);
        if (ch == '\n') break;
        buff[idx++] = ch;
    }
    buff[idx] = '\0';
    return buff;
}
char* __mx_builtin_toString(int n) {
    char* buff = (char*) malloc(16);
    sprintf(buff, "%d", n);
    return buff;
}