package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.behaviours.ShareMapBehaviour;


import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ChaserBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	
	private MapRepresentation myMap;
	
	private boolean koth=false;
	
	private String AmbushPoint;

	
	private String objective;

	private String lastKnownPosition;
	
	private int tick;

	private List<String> listAmbushAgentPoints;
	
	private int maxiter=200;
	
	private boolean move;

	public ChaserBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, String lastKnownPosition) {
		super(myagent);
		this.myMap=myMap;
		this.lastKnownPosition=lastKnownPosition;
		this.move=true;
		
		//ici boucle pour determiner si le sevice chasse est vide
		//TODO
		if (...) {
			this.koth=true;
			System.out.println("I, "+this.myAgent.getLocalName()+" am the king of the hunt");
			
			//ici obtention du nombre total d'agents (via somme nbAgents en service chasse + nbAgents en service explo ? )
			//TODO
			int nbAgents=...;
			this.AmbushPoint=myMap.getAmbushPoint(nbAgents);
			System.out.println("We will try to ambush the target at point "+this.AmbushPoint);
			this.tick=0;
			this.listAmbushAgentPoints=this.myMap.getSurroundingPoints(this.AmbushPoint);
			
			//on retire le point le plus proche pour laisser un passage au golem
			List<Integer> distances=new ArrayList<Integer>();
			for (int i=0;i<this.listAmbushAgentPoints.size();i++) {
				String point=this.listAmbushAgentPoints.get(i);
				distances.add(this.myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),point).size());
			}
			int argmin=-1;
			int min=Integer.MAX_VALUE;
			for (int i=0;i<distances.size();i++) {
				if (distances.get(i)<min) {
					min=distances.get(i);
					argmin=i;
				}
			}
			this.listAmbushAgentPoints.remove(argmin);
			
		}
		
		
	}

	@Override
	public void action() {
		
		
		//ticker management
		this.tick+=1;
		if (this.tick>=this.maxiter) {
			System.out.println("It takes to much time, let's try another ambush point.");
			
			//ici obtention du nombre total d'agents (via somme nbAgents en service chasse + nbAgents en service explo ? )
			//TODO
			int nbAgents=...;
			this.AmbushPoint=myMap.getAmbushPoint(nbAgents-1);
			System.out.println("We will try to ambush the target at point "+this.AmbushPoint);
			this.tick=0;
			this.listAmbushAgentPoints=this.myMap.getSurroundingPoints(this.AmbushPoint);
			
			//on retire le point le plus proche pour laisser un passage au golem
			List<Integer> distances=new ArrayList<Integer>();
			for (int i=0;i<this.listAmbushAgentPoints.size();i++) {
				String point=this.listAmbushAgentPoints.get(i);
				distances.add(this.myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),point).size());
			}
			int argmin=-1;
			int min=Integer.MAX_VALUE;
			for (int i=0;i<distances.size();i++) {
				if (distances.get(i)<min) {
					min=distances.get(i);
					argmin=i;
				}
			}
			this.listAmbushAgentPoints.remove(argmin);
		}
			
		
		
		//golem sniffing
		List<Couple<String, List<Couple<Observation, Integer>>>> odor = ((AbstractDedaleAgent) this.myAgent).observe();
		for (int i=0; i<odor.size();i++) {
			Couple<String, List<Couple<Observation, Integer>>> data = odor.get(i);
			String pos = data.getLeft();
			List<Couple<Observation, Integer>> l = data.getRight();
			for (int j=0;j<l.size();j++) {
				Couple<Observation, Integer> da = l.get(j);
				Observation obs = da.getLeft();
				if (obs.getName().compareTo("STENCH")==0){
					this.lastKnownPosition=pos;
				}
			}
		}
		
		
		//messages
		if (koth==true){		
			if (listAmbushAgentPoints.size()>0) {
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("Chase");
				msg.setContent("need_ambushers");
				
				//ici ajouter tous les agents en chasse en receveurs
				//TODO
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
		}
		
		
		MessageTemplate msgTemplate=MessageTemplate.and(MessageTemplate.MatchProtocol("Chase"),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			String sgreceived=null;
			try {
				sgreceived = (String)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (sgreceived.compareTo("need_ambushers")==0) {
				this.move=false;
				AID sender = msgReceived.getSender();
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("Chase");
				msg.setContent("here");
				msg.addReceiver(sender);				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
			}
			else {
				if (sgreceived.compareTo("here")==0) {
					if (listAmbushAgentPoints.size()>0) {
						AID sender = msgReceived.getSender();
						ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());
						msg.setProtocol("Chase");
						
						//donner le point le plus loin pour essayer d'éviter les interblocages
						List<Integer> distances=new ArrayList<Integer>();
						for (int i=0;i<listAmbushAgentPoints.size();i++) {
							String point=listAmbushAgentPoints.get(i);
							distances.add(this.myMap.getShortestPath(((AbstractDedaleAgent)this.myAgent).getCurrentPosition(),point).size());
						}
						int argmax=-1;
						int max=-1;
						for (int i=0;i<distances.size();i++) {
							if (distances.get(i)>max) {
								max=distances.get(i);
								argmax=i;
							}
						}
						String poen=listAmbushAgentPoints.remove(argmax);
						msg.setContent(poen);
						msg.addReceiver(sender);				
						((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					}
					else {
						AID sender = msgReceived.getSender();
						ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());
						msg.setProtocol("Chase");
						msg.setContent("not_needed");
						msg.addReceiver(sender);				
						((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					}
				}
				else {
					if (sgreceived.compareTo("not_needed")==0) {
						this.move=true;
						
					}
					else {
						String objectivepoint=sgreceived;
						this.myAgent.addBehaviour(new AmbusherBehaviour((AbstractDedaleAgent) this.myAgent,this.myMap,objectivepoint, this.tick, this.maxiter));
						this.finished=true; 
						}
					}
					
				}
			}
		
		//movement
		if (this.lastKnownPosition==null){
			//random movement
			
			//Random move from the current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			Random r= new Random();
			int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move

			//The move action (if any) should be the last action of your behaviour
			((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
		}
		else {
			
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			List<String> route = this.myMap.getShortestPath(myPosition,this.lastKnownPosition);
			if (route.size()==0) {
				System.out.println("I am at my last known position of the golem, time to go random");
				this.lastKnownPosition=null;
			}
			else {
				String nextNode = route.get(0);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			}
			
		}
		
	}

	@Override
	public boolean done() {
		return finished;
	}

}
