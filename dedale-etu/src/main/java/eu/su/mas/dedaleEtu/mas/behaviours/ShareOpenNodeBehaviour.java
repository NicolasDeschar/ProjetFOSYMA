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

public class ShareOpenNodeBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2757238453172963607L;
	private MapRepresentation myMap;



	public ShareOpenNodeBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap) {
		super(myagent);
		this.myMap=myMap;


		
	}
	@Override
	public void action() {
		System.out.println("testshareOpenNodes");
		MessageTemplate msgTemplate=MessageTemplate.and(
				MessageTemplate.MatchProtocol("BeginShareNodes"),
				MessageTemplate.MatchPerformative(ACLMessage.INFORM));
		ACLMessage msgReceived=this.myAgent.receive(msgTemplate);
		
		if(msgReceived!=null) {
			AID agentAID=msgReceived.getSender();
			List<String> openNodes = this.myMap.getOpenNodes();
			ArrayList<String> nodesToExplore = new ArrayList<String>();
			for (int i =0;i<openNodes.size();i++) {
				if(i%2==0) {
					this.myMap.addNode(openNodes.get(i), MapAttribute.shared);
				}else {
					nodesToExplore.add(openNodes.get(i));
				}
				
			}
		
			final ACLMessage msg1 = new ACLMessage(ACLMessage.INFORM);
		
			msg1.setSender(this.myAgent.getAID());

			msg1.addReceiver(agentAID);
		
			msg1.setProtocol("shareNodesToClose");
		
			try {
				msg1.setContentObject(nodesToExplore);
			} catch (IOException e) {
			// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			((AbstractDedaleAgent)this.myAgent).sendMessage(msg1);
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
