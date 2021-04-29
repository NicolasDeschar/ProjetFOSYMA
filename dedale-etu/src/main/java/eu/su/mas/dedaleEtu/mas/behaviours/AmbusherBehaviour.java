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

public class AmbusherBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	
	private MapRepresentation myMap;
	
	private int ticker;
	
	private int time_limit;
	
	private boolean accomplished;
	
	private boolean timed;

	
	private String objective;

	public AmbusherBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,String objective, int ticker, int time_limit) {
		super(myagent);
		this.myMap=myMap;
		this.objective=objective;
		this.ticker=ticker;
		this.time_limit=time_limit;
		this.accomplished=false;
		this.timed=true;
		
		
	}

	@Override
	public void action() {
		if (timed) {
			this.ticker+=1;
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
			if (sgreceived.compareTo("is_golem_blocked")==0) {
				AID sender = msgReceived.getSender();
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("Chase");
				boolean y=false;
				List<Couple<String, List<Couple<Observation, Integer>>>> odor = ((AbstractDedaleAgent) this.myAgent).observe();
				for (int i=0; i<odor.size();i++) {
					Couple<String, List<Couple<Observation, Integer>>> data = odor.get(i);
					String pos = data.getLeft();
					List<Couple<Observation, Integer>> l = data.getRight();
					for (int j=0;j<l.size();j++) {
						Couple<Observation, Integer> da = l.get(j);
						Observation obs = da.getLeft();
						if (obs.getName().compareTo("STENCH")==0){
							y=true;
							}
						}
					}
				if (y) {
					msg.setContent("seems_to_me");
				}
				else {
					msg.setContent("nope");
				}
				msg.addReceiver(sender);				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
			else {
				if (sgreceived.compareTo("he_s_done")==0) {
					this.timed=false;
				}
			}
		}
				
		
		
		if (this.ticker>=this.time_limit) {
			this.myAgent.addBehaviour(new ChaserBehaviour((AbstractDedaleAgent) this.myAgent,this.myMap, null, -1));
			this.finished=true;
		}

		if(this.myMap==null) {
			this.myMap= new MapRepresentation();
		if (!accomplished) {
			String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
			List<String> route = this.myMap.getShortestPath(myPosition,objective);
			if (route.size()==0) {
				System.out.println("I am at the objective point");
				this.accomplished=true;
			}
			else {
				String nextNode = route.get(0);
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}
			}
		}
	}

	@Override
	public boolean done() {
		return finished;
	}

}
