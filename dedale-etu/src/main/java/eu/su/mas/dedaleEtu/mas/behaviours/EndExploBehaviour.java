package eu.su.mas.dedaleEtu.mas.behaviours;

import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class EndExploBehaviour extends OneShotBehaviour{
	// cette classe est utilisée à la fin de l'exploration de carte, on inscrit l'agent au service chasse et on commence la behaviour de chasse
	/**
	 * 
	 */
	private static final long serialVersionUID = -4205918489017546675L;
	private Agent myAgent;
	public EndExploBehaviour(Agent myAgent) {
		this.myAgent=myAgent;
	}
	@Override
	public void action() {
		System.out.println("End");
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(myAgent.getAID());
		try {
			DFService.deregister(this.myAgent);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ServiceDescription sd = new ServiceDescription();
		sd.setType( "CHASSE" ); 

		sd.setName(myAgent.getLocalName());

		dfd.addServices(sd);

		try{DFService. register ( this.myAgent , dfd );} 
		catch (FIPAException fe) {fe . printStackTrace(); }
		((ExploreCoopAgent) this.myAgent).endExplo();
		this.myAgent.addBehaviour( new ChaserBehaviour(this.myAgent,((ExploreCoopAgent) this.myAgent).getMap(),((ExploreCoopAgent) this.myAgent).getLastKnownPosition()));
		
	}
	
	

}
