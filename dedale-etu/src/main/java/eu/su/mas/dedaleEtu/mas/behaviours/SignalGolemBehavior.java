package eu.su.mas.dedaleEtu.mas.behaviours;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * This example behaviour try to send a hello message (every 3s maximum) to agents Collect2 Collect1
 * @author hc
 *
 */
public class SignalGolemBehavior extends TickerBehaviour{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2058134622078521998L;
	private List<String> list_agentNames;

	/**
	 * An agent tries to contact its friend and to give him its current position
	 * @param myagent the agent who posses the behaviour
	 *  
	 */
	public SignalGolemBehavior (final Agent myagent, ExploCoopBehaviour exlpo) {
		super(myagent, 3000);
		//super(myagent);
	}

	@Override
	public void onTick() {
		
		List<Couple<String, List<Couple<Observation, Integer>>>> odor = ((AbstractDedaleAgent) this.myAgent).observe();
		for (int i=0; i<odor.size();i++) {
			Couple<String, List<Couple<Observation, Integer>>> data = odor.get(i);
			String pos = data.getLeft();
			List<Couple<Observation, Integer>> l = data.getRight();
			for (int j=0;j<l.size();j++) {
				Couple<Observation, Integer> da = l.get(j);
				Observation obs = da.getLeft();
				if (obs.getName().compareTo("STENCH")==0){
					ACLMessage msg=new ACLMessage(ACLMessage.INFORM);
					msg.setSender(this.myAgent.getAID());
					msg.setProtocol("Golem_found");
					try {
						msg.setContentObject(pos);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//boucle temporaire
					if (this.myAgent.getLocalName().equals("Explo1")) {
						list_agentNames = Arrays.asList("Explo2");
					}else {
						list_agentNames = Arrays.asList("Explo1");
					}
					for (int k=0;k<list_agentNames.size();k++) {
						msg.addReceiver(new AID(list_agentNames.get(k),AID.ISLOCALNAME));
					}
					((AbstractDedaleAgent)this.myAgent).sendMessage(msg);
					
				}
			}
		}
	}
		
}