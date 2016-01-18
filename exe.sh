#!/bin/bash

rm -rf res*
rm -rf examples/org/cloudbus/cloudsim/examples/power/*class examples/org/cloudbus/cloudsim/examples/power/planetlab/*class

case "$1" in
1)  
	javac -classpath jars/cloudsim-new.jar:examples examples/org/cloudbus/cloudsim/examples/power/planetlab/Dvfs.java
	java -classpath jars/cloudsim-new.jar:examples org.cloudbus.cloudsim.examples.power.planetlab.Dvfs > res
	cat res | grep Coverage > res1
	;;
2)  
	javac -classpath jars/cloudsim-new.jar:examples examples/org/cloudbus/cloudsim/examples/power/planetlab/MadMmt.java
	java -classpath jars/cloudsim-new.jar:examples org.cloudbus.cloudsim.examples.power.planetlab.MadMmt > res
	cat res | grep Coverage > res1
	;;
3)  
	javac -classpath jars/cloudsim-new.jar:examples examples/org/cloudbus/cloudsim/examples/power/planetlab/IqrRs.java
	java -classpath jars/cloudsim-new.jar:examples org.cloudbus.cloudsim.examples.power.planetlab.IqrRs > res
	cat res | grep Coverage > res1
	;;
4) 
	javac -classpath jars/cloudsim-new.jar:examples examples/org/cloudbus/cloudsim/examples/power/planetlab/LrMu.java
	java -classpath jars/cloudsim-new.jar:examples org.cloudbus.cloudsim.examples.power.planetlab.LrMu > res
	cat res | grep Coverage > res1
	;;
*) 
	;;
esac
