package com.serverless.demo.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.InstanceType;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.TerminateInstancesRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Serverless code to launch, start, stop and terminate EC2 instance. 
 */

public class HelloWorld implements RequestHandler<Object, String> {
	final AmazonEC2 ec2 = AmazonEC2ClientBuilder.defaultClient();

	@Override
	public String handleRequest(Object input, Context context) {

		List<Instance> instancesList = getEC2Instances();

		System.out.println("Found total ec2 instances - "+instancesList.size());
		if (!instancesList.isEmpty()) {
		  System.out.println("Stopping instances...");
			for (Instance instanceOpt : instancesList) {
				System.out.println("Instance state - "+ instanceOpt.getState().getName());
					stopEC2Instance(instanceOpt);
					terminateEC2Instance(instanceOpt);
			}
		}else {
			System.out.println("Launching instances...");
			 launchEC2Instance();
		}

		return "Done with execution for EC2 instance!";
	}

	private List<Instance> getEC2Instances() {

		List<Instance> instances = new ArrayList<>();

		boolean done = false;

		DescribeInstancesRequest request = new DescribeInstancesRequest();
		while (!done) {
			DescribeInstancesResult response = ec2.describeInstances(request);

			for (Reservation reservation : response.getReservations()) {
				for (Instance instance : reservation.getInstances()) {
					System.out.printf(
							"Found instance with id %s, " + "AMI %s, " + "type %s, " + "state %s "
									+ "and monitoring state %s",
							instance.getInstanceId(), instance.getImageId(), instance.getInstanceType(),
							instance.getState().getName(), instance.getMonitoring().getState());

					instances.add(instance);
					System.out.println("---------------------");				
				}
			}

			request.setNextToken(response.getNextToken());

			if (response.getNextToken() == null) {
				done = true;
			}
		}

		return instances;
	}

	public String launchEC2Instance() {
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		runInstancesRequest.withImageId("ami-0c6b1d09930fac512").withInstanceType(InstanceType.T2Micro).withMinCount(1)
				.withMaxCount(1).withKeyName("aws-vaibhav-ec2-key");
		// .withSecurityGroups("my-security-group");
		System.out.println("Started launching instance");

		RunInstancesResult run_response = ec2.runInstances(runInstancesRequest);

		System.out.println("Run response - " + run_response.toString());

		String reservation_id = run_response.getReservation().getInstances().get(0).getInstanceId();
		System.out.println("Instance/Reversion id = " + reservation_id);
		return reservation_id;
	}

	public void stopEC2Instance(Instance instance) {
		String instance_id = instance.getInstanceId();
		System.out.println("before stopping ec2 instance id - "+instance_id+" and state - "+instance.getState().getName());
		System.out.println("stop check - "+(instance.getState().getName().equalsIgnoreCase("running")) );
		if(instance.getState().getName().equalsIgnoreCase("running")) {
			StopInstancesRequest request = new StopInstancesRequest().withInstanceIds(instance_id);
			ec2.stopInstances(request);
			System.out.println("EC2 instance- " + instance_id + " stopped successfully.");
		}
	}

	public void startEC2Instance(Instance instance) {
		String instance_id = instance.getInstanceId();
		System.out.println("before starting ec2 instance id - "+instance_id+" and state - "+instance.getState().getName());
		System.out.println("start check - "+(instance.getState().getName().equalsIgnoreCase("stopped")));
		if(instance.getState().getName().equalsIgnoreCase("stopped")){
			StartInstancesRequest request = new StartInstancesRequest().withInstanceIds(instance_id);
			ec2.startInstances(request);
			System.out.println("EC2 instance- " + instance_id + " started successfully.");
		}
	}
	
	public void terminateEC2Instance(Instance instance) {
		String instance_id = instance.getInstanceId();
		System.out.println("before terminating ec2 instance id - "+instance_id+" and state - "+instance.getState().getName());
		System.out.println("terminate check - "+(!(instance.getState().getName().equalsIgnoreCase("terminated"))));
		if(!(instance.getState().getName().equalsIgnoreCase("terminated"))){
			TerminateInstancesRequest terminateInstancesRequest = new TerminateInstancesRequest().withInstanceIds(instance_id);
			System.out.println("Terminating Ec2 instance - "+ec2.terminateInstances(terminateInstancesRequest));
		}
	}

}
