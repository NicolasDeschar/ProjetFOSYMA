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
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ChaserBehaviour extends SimpleBehaviour {

	private static final long serialVersionUID = 8567689731496787661L;

	private boolean finished = false;
	
	private MapRepresentation myMap;
	
	private boolean koth=false;
	
	private List<String> AmbushPoint;

	
	private String objective;

	private String lastKnownPosition;
	
	private int tick;

	private List<String> listAmbushAgentPoints;
	
	private int maxiter=200;
	
	private boolean move;

	private List<AID> list_agentNames_chasse;

	private List<AID> list_agentNames_explo;
	
	private int nbAgents;

	private int nbGolem;
	
	private int mode;
	
	private String oldpos;
	
	private List<AID> listSenders;

	private List<AID> listblockers;
	
	private String blok;

	public ChaserBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, String lastKnownPosition, int nbGolem) {
		super(myagent);
		this.myMap=myMap;
		this.lastKnownPosition=lastKnownPosition;
		this.move=true;
		this.list_agentNames_chasse=new ArrayList<AID>();
		this.list_agentNames_explo=new ArrayList<AID>();
		this.nbGolem=nbGolem;
		this.mode=1;
		this.oldpos=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		this.listblockers=new ArrayList<AID>();
		
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();sd.setType( "CHASSE" ); 
		dfd.addServices(sd);
		DFAgentDescription[] result=null;
		try {
			result=DFService.search(this.myAgent , dfd);
			} catch (FIPAException e) {
				e.printStackTrace();
				}
		for(int i=0;i<result.length;i++) {
			this.list_agentNames_chasse.add(result[i].getName());
			}
		
		DFAgentDescription dfd1 = new DFAgentDescription();
		ServiceDescription sd1 = new ServiceDescription();sd1.setType( "EXPLORATION" ); 
		dfd1.addServices(sd1);
		DFAgentDescription[] result1=null;
		try {
			result1=DFService.search(this.myAgent , dfd1);
			} catch (FIPAException e) {
				e.printStackTrace();
				}
		for(int i=0;i<result1.length;i++) {
			this.list_agentNames_explo.add(result1[i].getName());
			}
		
		this.nbAgents=this.list_agentNames_explo.size()+this.list_agentNames_chasse.size();
		
		if ((this.list_agentNames_chasse.size()==1)&&(this.list_agentNames_chasse.get(0)==this.myAgent.getAID())) {
			this.koth=true;
			System.out.println("I, "+this.myAgent.getLocalName()+" am the king of the hunt");
			
			this.AmbushPoint=myMap.getAmbushPoint(nbAgents,this.nbGolem, this.mode);
			System.out.println("We will try to ambush the target at point "+this.AmbushPoint.toString());
			this.tick=0;
			this.listAmbushAgentPoints=new ArrayList<String>();
			for (int j=0;j<this.AmbushPoint.size();j++) {
				List<String> tempora = this.myMap.getSurroundingPoints(this.AmbushPoint.get(j));
				
				//on retire le point le plus proche pour laisser un passage au golem
				List<Integer> distances=new ArrayList<Integer>();
				for (int i=0;i<tempora.size();i++) {
					String point=tempora.get(i);
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
				tempora.remove(argmin);
				this.listAmbushAgentPoints.addAll(tempora);
			}	
		}
		
	}

	@Override
	public void action() {
		
		
		//critère d'arrêt
		if (((AbstractDedaleAgent)this.myAgent).getCurrentPosition()==this.oldpos) {
			boolean gol=false;
			List<Couple<String, List<Couple<Observation, Integer>>>> odor = ((AbstractDedaleAgent) this.myAgent).observe();
			for (int i=0; i<odor.size();i++) {
				Couple<String, List<Couple<Observation, Integer>>> data = odor.get(i);
				String pos = data.getLeft();
				List<Couple<Observation, Integer>> l = data.getRight();
				for (int j=0;j<l.size();j++) {
					Couple<Observation, Integer> da = l.get(j);
					Observation obs = da.getLeft();
					if (obs.getName().compareTo("STENCH")==0){
						gol=true;
						this.blok=pos;
					}
				}
			}
			if (gol) {
				this.listblockers=new ArrayList<AID>();
				System.out.println("Seems that I am blocked by the golem, maybe he's trapped");
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setSender(this.myAgent.getAID());
				msg.setProtocol("Chase");
				msg.setContent("is_golem_blocked");
				
				this.list_agentNames_chasse=new ArrayList();				
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();sd.setType( "CHASSE" ); 
				dfd.addServices(sd);
				DFAgentDescription[] result=null;
				try {
					result=DFService.search(this.myAgent , dfd);
					} catch (FIPAException e) {
						e.printStackTrace();
						}
				for(int i=0;i<result.length;i++) {
					this.list_agentNames_chasse.add(result[i].getName());
					}
				
				for (int u=0;u<this.list_agentNames_chasse.size();u++) {
					msg.addReceiver(this.list_agentNames_chasse.get(u));
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
			}
		}
		
		
		
		
		//ticker management
		this.tick+=1;
		if ((this.tick>=this.maxiter)&&(koth==true)) {
			System.out.println("It takes to much time, let's try another ambush point.");
			this.AmbushPoint=myMap.getAmbushPoint(nbAgents,this.nbGolem, this.mode);
			System.out.println("We will try to ambush the target at point "+this.AmbushPoint.toString());
			this.tick=0;
			this.listAmbushAgentPoints=new ArrayList<String>();
			for (int j=0;j<this.AmbushPoint.size();j++) {
				List<String> tempora = this.myMap.getSurroundingPoints(this.AmbushPoint.get(j));
				
				//on retire le point le plus proche pour laisser un passage au golem
				List<Integer> distances=new ArrayList<Integer>();
				for (int i=0;i<tempora.size();i++) {
					String point=tempora.get(i);
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
				tempora.remove(argmin);
				this.listAmbushAgentPoints.addAll(tempora);
			}
		}
			
		
		
		//golem sniffing
		List<Couple<String, List<Couple<Observation, Integer>>>> odor1 = ((AbstractDedaleAgent) this.myAgent).observe();
		for (int i=0; i<odor1.size();i++) {
			Couple<String, List<Couple<Observation, Integer>>> data = odor1.get(i);
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
				
				this.list_agentNames_chasse=new ArrayList();				
				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();sd.setType( "CHASSE" ); 
				dfd.addServices(sd);
				DFAgentDescription[] result=null;
				try {
					result=DFService.search(this.myAgent , dfd);
					} catch (FIPAException e) {
						e.printStackTrace();
						}
				for(int i=0;i<result.length;i++) {
					this.list_agentNames_chasse.add(result[i].getName());
					}
				
				for (int u=0;u<this.list_agentNames_chasse.size();u++) {
					msg.addReceiver(this.list_agentNames_chasse.get(u));
				}
				
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
						if (sgreceived.compareTo("is_golem_blocked")==0) {
							assert true;							
						}
						else {
							if (sgreceived.compareTo("nope")==0) {
								this.listblockers=new ArrayList<AID>();
							}
							else {
								if (sgreceived.compareTo("seems_to_me")==0) {
									this.listblockers.add(msgReceived.getSender());
									if (this.listblockers.size()>=this.myMap.getSurroundingPoints(this.blok).size()) {
										System.out.println("The golem is blocked, stop moving");
										ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
										msg.setSender(this.myAgent.getAID());
										msg.setProtocol("Chase");
										msg.setContent("he's_done");
										for (int i=0;i<this.listblockers.size();i++) {
											msg.addReceiver(this.listblockers.get(i));
										}
										((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
										}
									}			
								else {
									String objectivepoint=sgreceived;
									this.myAgent.addBehaviour(new AmbusherBehaviour((AbstractDedaleAgent) this.myAgent,this.myMap,objectivepoint, this.tick, this.maxiter));
									this.finished=true; 
									}
								}
							}
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