package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;

public class ReceiveOpenNodesBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4364673007704855413L;
	private MapRepresentation myMap;



	public ReceiveOpenNodesBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;


		
	}
	@Override
	public void action() {
		
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("shareNodesToClose"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if(msgReceived!=null) {
			System.out.println("testreceiveopennodes");
			AID agentAID=msgReceived.getSender();
			ArrayList<String> nodesToClose = new ArrayList<String>();
			try {
				nodesToClose = (ArrayList<String>) msgReceived.getContentObject();
			} catch (UnreadableException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String i : nodesToClose) {
				myMap.addNode(i, MapAttribute.shared);
			}
			
			this.myMap.setLastMeetingSpot(agentAID, ((AbstractDedaleAgent)this.myAgent).getCurrentPosition());
		}else {
			this.block();
		}

	}


	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
