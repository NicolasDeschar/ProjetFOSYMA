package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.agents.dummies.explo.ExploreCoopAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class ExploBehaviour extends OneShotBehaviour {
	private ExploreCoopAgent myAgent;
	private boolean finished;
	private int value=0;
	public ExploBehaviour(ExploreCoopAgent exploreCoopAgent) {
		this.myAgent=exploreCoopAgent;
	}

	@Override
	public void action() {
		this.myAgent.doWait(1000);

		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();
		if(((ExploreCoopAgent)this.myAgent).getPriority().contains(myPosition)) {
			List<String> p = ((ExploreCoopAgent)this.myAgent).getPriority();
			p.remove(myPosition);
			((ExploreCoopAgent)this.myAgent).setPriority(p);
		}
		
		if (myPosition!=null){
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();
			List<Couple<String, List<Couple<Observation, Integer>>>> odor = ((AbstractDedaleAgent) this.myAgent).observe();
	        for (int i=0; i<odor.size();i++) {
	            Couple<String, List<Couple<Observation, Integer>>> data = odor.get(i);
	            String pos = data.getLeft();
	            List<Couple<Observation, Integer>> l = data.getRight();
	            for (int j=0;j<l.size();j++) {
	                Couple<Observation, Integer> da = l.get(j);
	                Observation obs = da.getLeft();
	                if (obs.getName().compareTo("Stench")==0){
	                    this.myAgent.setLastKnowPosition(pos);
	                }
	            }
	        }


			//1) remove the current node from openlist and add it to closedNodes.
			this.myAgent.getMap().addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			if(((ExploreCoopAgent)this.myAgent).getPriority().size()>0) {
				if(this.myAgent.getMap().getShortestPath(myPosition,((ExploreCoopAgent)this.myAgent).getPriority().get(0))!=null) {
					nextNode=this.myAgent.getMap().getShortestPath(myPosition,((ExploreCoopAgent)this.myAgent).getPriority().get(0)).get(0);
					System.out.println(this.myAgent.getLocalName()+"-- Priority list= "+((ExploreCoopAgent)this.myAgent).getPriority()+"| nextNode: "+nextNode);
			
				}
			}
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myAgent.getMap().addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myAgent.getMap().addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			//3) while openNodes is not empty, continues.
			if (!this.myAgent.getMap().hasOpenNode()){
				//Explo finished
				value=3;
				finished=true;
				

			}else{

				//4) select next move.
				//4.1 If there exist one open node directly reachable, go for it,
				//	 otherwise choose one from the openNode list, compute the shortestPath and go for it
				if (nextNode==null){
					//no directly accessible openNode and no node in the priority list
					//chose one, compute the path and take the first step.
					System.out.println("goal:"+this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition).get( this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition).size()-1));
					nextNode=this.myAgent.getMap().getShortestPathToClosestOpenNode(myPosition).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
					System.out.println(this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"| nextNode: "+nextNode);
				}else {
					System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myAgent.getMap().getOpenNodes()+"\n -- nextNode: "+nextNode);
				}
				((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);


			}
		}

		
		
	}
	public int onEnd() {
		return value;	
	}

}
