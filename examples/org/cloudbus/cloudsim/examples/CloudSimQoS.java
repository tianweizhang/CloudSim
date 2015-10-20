package org.cloudbus.cloudsim.examples;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmAllocationPolicyQoS;
import org.cloudbus.cloudsim.VmAllocationPolicyUtil;
import org.cloudbus.cloudsim.VmAllocationPolicyRandom;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class CloudSimQoS {
	private static List<Cloudlet> cloudletList;
	private static List<Vm> vmList;
	private static List<Vm> createVM(int userId, int vms, int idShift) {
		LinkedList<Vm> list = new LinkedList<Vm>();
		long size = 10000; //image size (MB)
		int ram = 512; //vm memory (MB)
		int mips = 25;
		long bw = 100;
		String vmm = "Xen"; //VMM name

		Vm[] vm = new Vm[vms];

		for(int i=0;i<vms;i++){
			int qos = 0;
			int pesNumber = 0;
			double ran1 = Math.random();
			double ran2 = Math.random();
			if (ran1 < 0.25) {
				pesNumber = 1;
			}
			else if (ran1 < 0.5) {
				pesNumber = 2;
			}
			else if (ran1 < 0.75) {
				pesNumber = 4;
			}
			else {
				pesNumber = 8;
			}

			if (ran2 < 0.1) {
				qos = 1;
			}
			else if (ran2 < 0.2) {
				qos = 2;
			}
			else if (ran2 < 0.3) {
				qos = 3;
			}
			else {
				qos = 4;
			}
			vm[i] = new Vm(idShift + i, userId, mips, pesNumber, qos, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
			list.add(vm[i]);
		}
		return list;
	}

	private static List<Cloudlet> createCloudlet(int userId, int cloudlets, int idShift){
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		long length = 40000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];

		for(int i=0;i<cloudlets;i++){
			cloudlet[i] = new Cloudlet(idShift + i, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	private static Datacenter createDatacenter(String name, int hosts, int vm_scheduler){
		List<Host> hostList = new ArrayList<Host>();
		List<Pe> peList = new ArrayList<Pe>();

		int mips = 1000;
		int num_cores = 16;
		for(int i=0; i<num_cores; i++){
			peList.add(new Pe(i, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		}

		int ram = 16384; //host memory (MB)
		long storage = 5000000; //host storage
		int bw = 10000;
		int qos = 32;

		for (int i=0; i<hosts; i++) {
			hostList.add(new Host(i, qos, new RamProvisionerSimple(ram), new BwProvisionerSimple(bw), storage, peList, new VmSchedulerTimeShared(peList))); 
		}

		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now
		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);

		Datacenter datacenter = null;
		switch(vm_scheduler) {
			case 0:
				try {
					datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyUtil(hostList), storageList, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 1:
				try {
					datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyRandom(hostList), storageList, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			case 2:
				try {
					datacenter = new Datacenter(name, characteristics, new VmAllocationPolicyQoS(hostList), storageList, 0);
				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
			default:
				break;
		}

		return datacenter;
	}

	private static DatacenterBroker createBroker(String name){
		DatacenterBroker broker = null;
		try {
			broker = new DatacenterBroker(name);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	public static void main(String[] args) {
		try {
			int num_vm = Integer.parseInt(args[0]);
			int vm_scheduler = Integer.parseInt(args[1]);
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			CloudSim.init(num_user, calendar, trace_flag);
			for (int i = 1; i<num_vm; i++) {
				GlobalBroker globalBroker = new GlobalBroker("GlobalBroker", i*10);
			}

			@SuppressWarnings("unused")
			Datacenter datacenter = createDatacenter("Datacenter", 100, vm_scheduler);

			DatacenterBroker broker = createBroker("Broker");
			int brokerId = broker.getId();

			vmList = createVM(brokerId, 10, 0); //creating 5 vms
			cloudletList = createCloudlet(brokerId, 20, 0); // creating 10 cloudlets

			broker.submitVmList(vmList);
			broker.submitCloudletList(cloudletList);

			CloudSim.startSimulation();

			CloudSim.stopSimulation();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static class GlobalBroker extends SimEntity {

		private static final int CREATE_BROKER = 0;
		private List<Vm> vmList;
		private List<Cloudlet> cloudletList;
		private DatacenterBroker broker;
		private int shift;
		public GlobalBroker(String name, int shift) {
			super(name);
			this.shift = shift;
		}

		@Override
		public void processEvent(SimEvent ev) {
			switch (ev.getTag()) {
			case CREATE_BROKER:
				setBroker(createBroker(super.getName()+"_"));

				setVmList(createVM(getBroker().getId(), 10, shift)); //creating 5 vms
				setCloudletList(createCloudlet(getBroker().getId(), 20, shift*2)); // creating 10 cloudlets

				broker.submitVmList(getVmList());
				broker.submitCloudletList(getCloudletList());

				CloudSim.resumeSimulation();

				break;

			default:
				break;
			}
		}
		@Override
		public void startEntity() {
			schedule(getId(), shift, CREATE_BROKER);
		}
		@Override
		public void shutdownEntity() {
		}
		public List<Vm> getVmList() {
			return vmList;
		}
		protected void setVmList(List<Vm> vmList) {
			this.vmList = vmList;
		}
		public List<Cloudlet> getCloudletList() {
			return cloudletList;
		}
		protected void setCloudletList(List<Cloudlet> cloudletList) {
			this.cloudletList = cloudletList;
		}
		public DatacenterBroker getBroker() {
			return broker;
		}
		protected void setBroker(DatacenterBroker broker) {
			this.broker = broker;
		}
	}
}
