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
	
	private boolean koth;
	
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

	private int ambusher_sent;
	
	private  int message_timer;

	public ChaserBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap, String lastKnownPosition, int nbGolem) {
		super(myagent);
		this.myMap=myMap;
		this.lastKnownPosition=lastKnownPosition;
		this.move=true;
		this.list_agentNames_chasse=new ArrayList<AID>();
		this.list_agentNames_explo=new ArrayList<AID>();
		this.nbGolem=nbGolem;
		this.mode=3;
		this.oldpos=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		this.listblockers=new ArrayList<AID>();
		this.koth=true;
		this.ambusher_sent=0;
		this.message_timer=0;
		System.out.println(this.myAgent.getLocalName()+", my last known position is "+this.lastKnownPosition);
		
		DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd = new ServiceDescription();sd.setType( "CHASSE" ); 
        dfd.addServices(sd);
        DFAgentDescription[] result=null;
        try {
             result=DFService.search(this.myAgent , dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }
        DFAgentDescription me = new DFAgentDescription();
        me.setName(this.myAgent.getAID());
        
        List<AID> list_agentNames = new ArrayList<AID>();
        for(int i=0;i<result.length;i++) {
            list_agentNames.add(result[i].getName());
        }
        list_agentNames.remove(me.getName());
        System.out.println(list_agentNames);
        this.list_agentNames_chasse=list_agentNames;
		
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
		
		this.nbAgents=this.list_agentNames_explo.size()+this.list_agentNames_chasse.size()+1;
		
		this.AmbushPoint=myMap.getAmbushPoint(nbAgents,this.nbGolem, this.mode);
		System.out.println(this.myAgent.getLocalName()+", We will try to ambush the target at point "+this.AmbushPoint.toString());
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
		System.out.println("For this, I will need "+this.listAmbushAgentPoints.size()+" agents to block points "+this.listAmbushAgentPoints.toString());
	}
		

	@Override
	public void action() {
		
		this.myAgent.doWait(1000);
		//critère d'arrêt
		String blockpos = null;
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
					if (obs.getName().compareTo("Stench")==0){
						gol=true;
						this.blok=pos;
						blockpos=pos;
					}
				}
			}
			if (gol) {
				if (this.myMap.getSurroundingPoints(blockpos).size()==1) {
					System.out.println(this.myAgent.getLocalName()+",The golem is blocked on a leaf");
				}
				else {
					this.listblockers=new ArrayList<AID>();
					System.out.println(this.myAgent.getLocalName()+", Seems that I am blocked by the golem, maybe he's trapped");
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
					DFAgentDescription me = new DFAgentDescription();
					me.setName(this.myAgent.getAID());
					
					List<AID> list_agentNames = new ArrayList<AID>();
					for(int i=0;i<result.length;i++) {
						list_agentNames.add(result[i].getName());
					}
					list_agentNames.remove(me.getName());
					System.out.println(this.myAgent.getLocalName()+"My agent list "+list_agentNames);
					this.list_agentNames_chasse=list_agentNames;

						
						
					
					for (int u=0;u<this.list_agentNames_chasse.size();u++) {
						msg.addReceiver(this.list_agentNames_chasse.get(u));
					}
					
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
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
				if (obs.getName().compareTo("Stench")==0){
					this.lastKnownPosition=pos;
					System.out.println(this.myAgent.getLocalName()+", found the golem");
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
				
				for (int k=0;k<this.list_agentNames_chasse.size();k++){
					String elem = this.list_agentNames_chasse.get(k).toString();
					if (elem.compareTo(this.myAgent.getAID().toString())==0) {
						this.list_agentNames_chasse.remove(k);
						k-=1;
					}
				}
				
				for (int u=0;u<this.list_agentNames_chasse.size();u++) {
					msg.addReceiver(this.list_agentNames_chasse.get(u));
				}
				
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				System.out.println(this.myAgent.getLocalName()+",send an ambusher request");
			}
		}
		
		
		MessageTemplate msgTemplate=MessageTemplate.and(MessageTemplate.MatchProtocol("Chase"),MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		if (msgReceived!=null) {
			String sgreceived=null;
			sgreceived = (String)msgReceived.getContent();
			if (sgreceived.compareTo("need_ambushers")==0) {
				System.out.println(this.myAgent.getLocalName()+", received an ambusher request");
				if (this.koth==true) {
					this.move=false;
					AID sender = msgReceived.getSender();
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("Chase");
					msg.setContent("T"+this.ambusher_sent);
					msg.addReceiver(sender);				
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);	
				}
				else {
					this.move=false;
					AID sender = msgReceived.getSender();
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("Chase");
					msg.setContent("here");
					msg.addReceiver(sender);				
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				}
					
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
						this.ambusher_sent+=1;
						this.move=true;
					}
					else {
						AID sender = msgReceived.getSender();
						ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
						msg.setSender(this.myAgent.getAID());
						msg.setProtocol("Chase");
						msg.setContent("not_needed");
						msg.addReceiver(sender);				
						((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
						this.move=true;
					}
				}
				else {
					if (sgreceived.compareTo("not_needed")==0) {
						this.move=true;
						
					}
					else {
						if ((sgreceived.compareTo("is_golem_blocked")==0)||sgreceived.compareTo("he's_done")==0) {
							assert true;							
						}
						else {
							if (sgreceived.compareTo("nope")==0) {
								this.listblockers=new ArrayList<AID>();
								this.move=true;
							}
							else {
								if (sgreceived.compareTo("seems_to_me")==0) {
									this.listblockers.add(msgReceived.getSender());
									if (this.listblockers.size()>=this.myMap.getSurroundingPoints(this.blok).size()) {
										System.out.println(this.myAgent.getLocalName()+", The golem is blocked, stop moving");
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
									if (sgreceived.charAt(0)=='T') {
										String sub=sgreceived.substring(1,sgreceived.length());
										int othersent= Integer.parseInt(sub);
										System.out.println("my ambusher count : "+this.ambusher_sent);
										System.out.println("the other's ambusher count "+ othersent);
										if (othersent>this.ambusher_sent) {
											this.koth=false;
											AID sender = msgReceived.getSender();
											ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
											msg.setSender(this.myAgent.getAID());
											msg.setProtocol("Chase");
											msg.setContent("you_lead");
											msg.addReceiver(sender);				
											((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
											this.move=true;
											System.out.println(this.myAgent.getLocalName()+" won't be the leader");
											
										}
										else {
											if (othersent<this.ambusher_sent) {
												this.move=true;
											}
											else {
												AID sender = msgReceived.getSender();
												ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
												msg.setSender(this.myAgent.getAID());
												msg.setProtocol("Chase");
												msg.setContent("A"+this.myAgent.getLocalName());
												msg.addReceiver(sender);				
												((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
												this.move=true;
											}
											
										}
										
									}
									else {
										if (sgreceived.charAt(0)=='A') {
											String sub1=sgreceived.substring(6,sgreceived.length());
											int otherID= Integer.parseInt(sub1);
											int myID=Integer.parseInt(this.myAgent.getLocalName().substring(5,this.myAgent.getLocalName().length()));
											if (otherID<myID) {
												this.koth=false;
												AID sender = msgReceived.getSender();
												ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
												msg.setSender(this.myAgent.getAID());
												msg.setProtocol("Chase");
												msg.setContent("you_lead");
												msg.addReceiver(sender);				
												((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
												this.move=true;
												System.out.println(this.myAgent.getLocalName()+" won't be the leader");
												
											}
											else {
												this.move=true;
												}
											}
										else {
											if (sgreceived.compareTo("you_lead")==0) {
												this.koth=true;
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
							}
						}
					}
				}
			}
		}
		
		//movement
		if (this.move) {
			if ((this.lastKnownPosition!=(((AbstractDedaleAgent)this.myAgent).getCurrentPosition()))&&(this.lastKnownPosition!=null)){
				String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				System.out.println(this.myAgent.getLocalName()+", I a at position "+myPosition+" and I need to go to "+this.lastKnownPosition);
				List<String> route = this.myMap.getShortestPath(myPosition,this.lastKnownPosition);
				if (route.size()==0) {
					System.out.println(this.myAgent.getLocalName()+", I am at my last known position of the golem, time to go random");
					this.lastKnownPosition=null;
				}
				else {
					String nextNode = route.get(0);
					System.out.println(this.myAgent.getLocalName()+", I am not moving randomly");
					this.oldpos=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
					((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
				}
				
			}
			else {
				//random movement
				System.out.println(this.myAgent.getLocalName()+", I am moving randomly");
				
				//Random move from the current position
				List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
				Random r= new Random();
				int moveId=1+r.nextInt(lobs.size()-1);//removing the current position from the list of target, not necessary as to stay is an action but allow quicker random move
	
				//The move action (if any) should be the last action of your behaviour
				this.oldpos=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
				((AbstractDedaleAgent)this.myAgent).moveTo(lobs.get(moveId).getLeft());
			}
		}
		else {
			System.out.println(this.myAgent.getLocalName()+",I have stopped moving, waiting for messages");
			this.message_timer+=1;
			if (this.message_timer%5==0) {
				System.out.println(this.myAgent.getLocalName()+",I have waited, but nothing came, better start moving again");
				this.move=true;
				this.message_timer=0;
				
			}
		}
		
	}

	@Override
	public boolean done() {
		return finished;
	}

}