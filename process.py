#!/usr/bin/python

import sys
import os
import string
import getopt
import argparse

vm_num = ""
mode = ""

"ant: compile the file"
"javac -classpath jars/cloudsim-new.jar:examples examples/org/cloudbus/cloudsim/examples/CloudSimQoS.java: compile CloudSimQoS"

def main(argv):
	num_host = 100

	x = 0.0
	y = 0.0
	for i in range(10):
		active = [0]*num_host
		qos = [0]*num_host

		os.popen("java -classpath jars/cloudsim-new.jar:examples org.cloudbus.cloudsim.examples.CloudSimQoS "+ vm_num + " " + mode)

		result = open("data.txt", 'r')
		vm_list = result.readlines()
		result.close()

		os.popen("rm -rf data.txt")

		num_core = 0

		for vm in vm_list:
			vm_value = vm.split()
			host_id = int(vm_value[2])
			qos_value = int(vm_value[3])
			active[host_id] = 1
			qos[host_id] += qos_value
			num_core += int(vm_value[4])

		for i in range(num_host):
			if qos[i] > 32:
				qos[i] -= 32
			else:
				qos[i] = 0

		x += 1.0*len(vm_list)/sum(active)
		y += 1.0*sum(qos)/num_core

	print "{0} {1}".format(x/10, y/10)

if __name__ == "__main__":

	parser = argparse.ArgumentParser()
	parser.add_argument("vm_num", help="num of vms")
	parser.add_argument("-m", "--mode", default="0",
			    help="vm scheduling policy")
	args = parser.parse_args()

	vm_num = args.vm_num
	mode = args.mode
	main(sys.argv[1:])
