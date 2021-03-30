package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.List;

import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

public class PingBehaviour extends TickerBehaviour {
	private List<String> receivers;
	public PingBehaviour(Agent a, long period,List<String> receivers) {
		super(a, period);
		this.receivers=receivers;
	}

	@Override
	protected void onTick() {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setProtocol("PING");
		msg.setSender(this.myAgent.getAID());
		for (String agentName : receivers) {
			msg.addReceiver(new AID(agentName,AID.ISLOCALNAME));
		}
		((AbstractDedaleAgent)myAgent).sendMessage(msg);
		System.out.println("ping");

	}

}
