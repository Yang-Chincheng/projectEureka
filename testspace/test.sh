#!/bin/bash
llvm-link $1.ll builtin.ll -o test.bc
clang test.bc -o test
./test < test.in
#rm test test.ll