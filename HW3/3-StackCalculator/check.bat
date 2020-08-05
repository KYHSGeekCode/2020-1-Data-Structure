@echo off

rem comfile
javac -encoding UTF-8 BigInteger.java

rem make folder
MKDIR my_output

echo "-execute your program-"

echo Start Measure %Time%

rem make output
for /l %%i in (1, 1, 100) do (
	java BigInteger < testset\input\%%i.txt > my_output\%%i.txt
)

rem fc /b "1.txt" "testset/output/1.txt"

rem compare output
for /l %%i in (1, 1, 100) do (
	fc my_output\%%i.txt testset\output\%%i.txt
)

echo Stop Measure %Time% 
pause