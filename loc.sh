#!/bin/sh

echo "Lines of code"
find src -name "*.clj" | xargs wc -l

echo

echo "Lines of test code"
find test -name "*.clj" | xargs wc -l
