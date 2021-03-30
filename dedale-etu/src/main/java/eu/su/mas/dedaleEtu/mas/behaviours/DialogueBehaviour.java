package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
public class DialogueBehaviour extends SimpleBehaviour {
	private AID partner;
	private MapRepresentation myMap;
	private Agent myAgent;
	private long period;
	private boolean finished=false;
	
	public DialogueBehaviour(Agent a, long period , MapRepresentation mymap) {
		this.myAgent=a;
		this.myMap=mymap;
		this.period=period;//durée à partir de laquelle on arrete d'attendre une réponse
	}
	

	@Override
	public void action() {
		if(! (((ExploreCoopAgent)myAgent).getPinged())) {
			MessageTemplate msgTemplate=MessageTemplate.MatchPerformative(ACLMessage.INFORM);
			ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate,period);
			if (msgReceived!=null&&msgReceived.getProtocol()=="PING") {
				System.out.println(myAgent.getLocalName()+" pinged");
				((ExploreCoopAgent)myAgent).setPinged(true);
				partner=msgReceived.getSender();
				ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
				msg.setProtocol("SHARE-MAP");
				msg.setSender(myAgent.getAID());
				try {
					System.out.println("ok");
					
					
					msg.setContentObject(myMap.getSerializableGraph());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg.addReceiver(partner);
				((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
				
			}else {				
				if(msgReceived!=null&&msgReceived.getProtocol()=="SHARE-MAP") {
					//merge + envoi des difference entre le merge et la map recue
					System.out.println(myAgent.getLocalName()+" received a map");
					partner=msgReceived.getSender();
					SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
					try {
						sgreceived = (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
					} catch (UnreadableException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					myMap.mergeMap(sgreceived);
					SerializableSimpleGraph<String, MapAttribute> sgPartial = myMap.getPartialGraph(sgreceived);
					ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
					msg.setProtocol("SHARE-PARTIAL-MAP");
					
					msg.setSender(myAgent.getAID());
					msg.addReceiver(partner);

				}else {
					this.block();
				}
			}
		}else {
			MessageTemplate msgTemplate=MessageTemplate.and(MessageTemplate.MatchProtocol("SHARE-PARTIAL-MAP"),
					MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
							MessageTemplate.MatchSender(partner)));
			ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate,period);
		
			if(msgReceived!=null) {
				System.out.println(myAgent.getLocalName()+" received a partial map");
				//merge les maps
				((ExploreCoopAgent)myAgent).setPinged(false);
				SerializableSimpleGraph<String, MapAttribute> sgreceived=null;
				try {
					sgreceived = (SerializableSimpleGraph<String, MapAttribute>) msgReceived.getContentObject();
				} catch (UnreadableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				myMap.mergeMap(sgreceived);
			}else {
				((ExploreCoopAgent)myAgent).setPinged(false);
				this.block();
			}
		
			
		}
	}
	public void setDone(boolean finished) {
		this.finished=finished;
	}
	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
