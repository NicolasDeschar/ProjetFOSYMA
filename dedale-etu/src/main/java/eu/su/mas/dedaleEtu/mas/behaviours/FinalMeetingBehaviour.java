package eu.su.mas.dedaleEtu.mas.behaviours;

import java.util.Iterator;
import java.util.List;

import dataStructures.tuple.Couple;
import eu.su.mas.dedale.env.Observation;
import eu.su.mas.dedale.mas.AbstractDedaleAgent;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation;
import eu.su.mas.dedaleEtu.mas.knowledge.MapRepresentation.MapAttribute;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;

public class FinalMeetingBehaviour extends SimpleBehaviour {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8578703414004958475L;
	private MapRepresentation myMap;
	private String goal;
	private AID goalAgent;
	private boolean finished;



	public FinalMeetingBehaviour(final AbstractDedaleAgent myagent, MapRepresentation myMap,String goal,AID goalAgent) {
		super(myagent);
		this.myMap=myMap;
		this.goal=goal;
		this.goalAgent=goalAgent;

		
	}
	@Override
	public void action() {

		String myPosition=((AbstractDedaleAgent)this.myAgent).getCurrentPosition();

		if (myPosition!=null){
			//List of observable from the agent's current position
			List<Couple<String,List<Couple<Observation,Integer>>>> lobs=((AbstractDedaleAgent)this.myAgent).observe();//myPosition

			/**
			 * Just added here to let you see what the agent is doing, otherwise he will be too quick
			 */
			try {
				this.myAgent.doWait(250);
			} catch (Exception e) {
				e.printStackTrace();
			}

			//1) remove the current node from openlist and add it to closedNodes.
			this.myMap.addNode(myPosition, MapAttribute.closed);

			//2) get the surrounding nodes and, if not in closedNodes, add them to open nodes.
			String nextNode=null;
			Iterator<Couple<String, List<Couple<Observation, Integer>>>> iter=lobs.iterator();
			while(iter.hasNext()){
				String nodeId=iter.next().getLeft();
				boolean isNewNode=this.myMap.addNewNode(nodeId);
				//the node may exist, but not necessarily the edge
				if (myPosition!=nodeId) {
					this.myMap.addEdge(myPosition, nodeId);
					if (nextNode==null && isNewNode) nextNode=nodeId;
				}
			}

			if (nextNode==null){
				//no directly accessible openNode
				//chose one, compute the path and take the first step.
				nextNode=this.myMap.getShortestPath(myPosition, goal).get(0);//getShortestPath(myPosition,this.openNodes.get(0)).get(0);
				if(nextNode==null) {
					this.finished=true;
					myAgent.addBehaviour(new WaitMeetingBehaviour(this.myAgent,5000,goalAgent, myMap));
				}
			}else {
				//System.out.println("nextNode notNUll - "+this.myAgent.getLocalName()+"-- list= "+this.myMap.getOpenNodes()+"\n -- nextNode: "+nextNode);
			}
			((AbstractDedaleAgent)this.myAgent).moveTo(nextNode);
			
			
			
		}	
		

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return finished;
	}

}
