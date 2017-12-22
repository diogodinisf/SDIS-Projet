#!/bin/bash

for i in `seq 1 5`
do
    sleep 1
	java -jar "Node/dist/Node.jar" $i &
done
