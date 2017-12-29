#!/bin/bash

for i in `seq 1 1`
do
	java -jar Node2/dist/Node2.jar 1 $i &
done
