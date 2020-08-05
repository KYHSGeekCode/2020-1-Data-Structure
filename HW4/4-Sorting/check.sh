#!/usr/bin bash
echo "-compile start-"

# Compile
javac SortingTest.java

mkdir -p my_output

start=$SECONDS

echo "-execute your program-"
for i in $(seq 1 50)
do
        # 무한루프를 방지하기 위해 input 당 시간제한 30초
        timeout 30 java SortingTest < testset/input/$i.txt > my_output/$i.txt
done

# testset의 실행에 소요된 시간
echo "Execution time : $(($SECONDS-start)) seconds"

echo "-print wrong answers-"

for i in $(seq 1 50)
do
        #결과가 정답과 다를 경우 그 위치를 출력
        cmp my_output/$i.txt testset/output/$i.txt

done
