#!/bin/bash

if [ $1 ] 
then
	num=$1
else
	num=1
fi

for i in `seq 0 $(($num - 2))`
do
	java -jar "Node/dist/Node.jar" $i &
	sleep 0.2
done

java -jar "Node/dist/Node.jar" $((i + 1))
