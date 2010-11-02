package erco.activations.tsp;

import java.util.PriorityQueue;

public class Config {
	
	private final PriorityQueue<TourElement> queue;	
	final int numNodes;
	final int[][] weights;
	
	int startNode;
	int nodesFromEnd;
	
	int minTourLength;
	int[] minTour;
	
	Config(int tspSize) {
		queue = new PriorityQueue<TourElement>();
		numNodes = tspSize;
		weights = new int[numNodes + 1][numNodes + 1];
		minTour = new int[numNodes + 1];
		minTourLength = Integer.MAX_VALUE;
		nodesFromEnd = 12;
	}
	
	TourElement getTour() {
		synchronized(queue) {
			return queue.remove();
		}		
	}

	public void enqueue(final TourElement newTour) {
		synchronized(queue) {
			queue.add(newTour);
		}		
	}
	
	public void setBest(final int curDist, final int[] path) {
		synchronized(this) {
//			System.err.printf("curDist: %d minTourLength: %d tour: %s\n", 
//			curDist, minTourLength, Arrays.toString(path));
			if(curDist < minTourLength) {
				System.arraycopy(path, 0, minTour, 0, minTour.length);
				minTourLength = curDist;
		
//				System.err.printf("  BEST SO FAR\n");
			}
		}		
	}

}
