#!/bin/bash

for i in `seq 1 150`
do
	java -jar "/home/eduardo/SDIS-Projet/Node/dist/Node.jar" SDIS-Projet/Node/src/node/Node.java $i &
done
