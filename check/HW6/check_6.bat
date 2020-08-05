@echo off
	
rem comfile
javac -encoding UTF-8 Subway.java

rem make folder
MKDIR my_output

echo "-execute your program-"

echo Start Measure %Time%

for /l %%i in (1, 1, 30) do (
	SETLOCAL EnableDelayedExpansion
	for /f "delims=" %%t in (testset\argument\%%i.txt) do (
		set a=%%t)
						
	java -Dfile.encoding=UTF-8 Subway !a! < testset\input\%%i.txt > my_output\%%i.txt
)
for /l %%i in (1, 31, 60) do (
	SETLOCAL EnableDelayedExpansion
	for /f "delims=" %%t in (testset\argument\%%i.txt) do (
		set b=%%t)
						
	java -Dfile.encoding=UTF-8 Subway !b! < testset\input\%%i.txt > my_output\%%i.txt
)


echo Stop Measure %Time% 

echo "-print wrong answer-"

rem compare output

for /l %%i in (1, 1, 60) do (
	fc my_output\%%i.txt testset\output\%%i.txt	
)


pause