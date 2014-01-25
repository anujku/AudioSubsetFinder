#!/bin/bash
javac *.java
jar cfvm arc5500.jar manifest.txt *.class
chmod +x arc5500.jar
cat stub.sh arc5500.jar > arc5500 && chmod +x arc5500
rm *.class
rm *.jar 
