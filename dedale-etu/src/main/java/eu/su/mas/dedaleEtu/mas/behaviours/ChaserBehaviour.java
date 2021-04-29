package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;

public class ChaserBehaviour extends Behaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2790023754311260159L;

	public ChaserBehaviour(Agent myAgent, MapRepresentation map, String lastKnownPosition) {
		this.myAgent=myAgent;
	}

	@Override
	public void action() {
		System.out.println("Chasse "+this.myAgent.getLocalName());

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return true;
	}

}
