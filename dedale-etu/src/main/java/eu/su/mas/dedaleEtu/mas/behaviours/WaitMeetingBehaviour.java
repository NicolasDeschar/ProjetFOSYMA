package eu.su.mas.dedaleEtu.mas.behaviours;

import dataStructures.serializableGraph.SerializableSimpleGraph;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.SimpleBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class WaitMeetingBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8107455832976212552L;
	private AID meet;
	private int counter;
	private long period;
	private MapRepresentation myMap;
	private boolean finished=false;

	public WaitMeetingBehaviour(Agent myAgent, long period,AID a, MapRepresentation myMap) {
		super(myAgent);
		// TODO Auto-generated constructor stub
		this.meet=a;
		this.period=period;
		this.myMap=myMap;

	}

	@Override
	public void action() {
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("SHARE-TOPO"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.blockingReceive(msgTemplate,period);
		System.out.println("testmeet");
		if (msgReceived!=null) {
			SerializableSimpleGraph<String, MapAttribute> sgreceived = null;
			try {
				sgreceived = (SerializableSimpleGraph<String, MapAttribute>)msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.myMap.mergeMap(sgreceived);
			ACLMessage msg= new ACLMessage(ACLMessage.INFORM);
			msg.setProtocol("BeginShareNodes");
			msg.setSender(this.myAgent.getAID());
			msg.addReceiver(msgReceived.getSender());
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg);

			this.myAgent.addBehaviour(new ExploCoopBehaviour(((AbstractDedaleAgent)this.myAgent), myMap, null));//rajouter la liste des agents restants

		}else {
			if(myMap.hasOpenNode()) {
				this.myAgent.addBehaviour(new ExploCoopBehaviour(((AbstractDedaleAgent)this.myAgent), myMap, null));//rajouter la liste des agents restants
				
			}else if(myMap.hasSharedNode()){
				
				AID goalAgent=(AID) myMap.getLastMeetingSpots().keySet().toArray()[0];
				String goal=myMap.getLastMeetingSpot(goalAgent);

				((AbstractDedaleAgent)this.myAgent).addBehaviour(new FinalMeetingBehaviour((AbstractDedaleAgent) this.myAgent, 
						myMap,goal,goalAgent));
			}
		}
		this.finished=true;
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}



}
