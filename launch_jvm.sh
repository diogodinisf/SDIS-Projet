#!/bin/bash

for i in `seq 0 5`
do
	java -jar "Node/dist/Node.jar" $i &
	sleep 1
done

java -jar "Node/dist/Node.jar" $((i + 1))
