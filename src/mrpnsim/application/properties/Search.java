package mrpnsim.application.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import javafx.util.Pair;
import mrpnsim.application.model.Marking;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;
import mrpnsim.application.simulator.Execution;
import mrpnsim.application.simulator.ForwardExecution;
import mrpnsim.application.simulator.MRPNUtils;
import mrpnsim.application.simulator.ReverseExecution;

public class Search {

	protected MRPN mrpn;
	protected MRPNUtils utils;
	protected ForwardExecution forward;
	protected ReverseExecution reverse;

	public Search(MRPN mrpn, ForwardExecution forward, ReverseExecution reverse) {
		super();
		this.mrpn = mrpn;
		this.utils = new MRPNUtils(mrpn);
		this.forward = forward;
		this.reverse = reverse;
	}

	Set<Node> getNeighbors(Node current) {

		mrpn.setMarking(current.marking);

		ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> forwardTransitions = forward.getEnabledTransitions();
		ArrayList<Pair<Transition, ArrayList<ArrayList<Token>>>> reverseTransitions = reverse.getEnabledTransitions();
		Set<Node> neighbors = new HashSet<Node>();

		for (Pair<Transition, ArrayList<ArrayList<Token>>> forwardTransition : forwardTransitions) {

			Transition transition = forwardTransition.getKey();
			ArrayList<ArrayList<Token>> matching = forwardTransition.getValue();

			for (int i = 0; i < matching.size(); i++) {
				Marking newMarking = new Marking(current.marking);
				mrpn.setMarking(newMarking);
				forward.fireTransition(transition, i + 1);

				Node neighbor = new Node(newMarking, current.depth + 1);
				Edge edge = new Edge(current, neighbor, transition, forward);

				current.children.add(edge);
				neighbor.fromParent = edge;

				neighbors.add(neighbor);
			}
		}

		for (Pair<Transition, ArrayList<ArrayList<Token>>> reverseTransition : reverseTransitions) {

			Transition transition = reverseTransition.getKey();
			ArrayList<ArrayList<Token>> matching = reverseTransition.getValue();

			for (int i = 0; i < matching.size(); i++) {
				Marking newMarking = new Marking(current.marking);
				mrpn.setMarking(newMarking);
				reverse.fireTransition(transition, i + 1);

				Node neighbor = new Node(newMarking, current.depth - 1);
				Edge edge = new Edge(current, neighbor, transition, reverse);

				current.children.add(edge);
				neighbor.fromParent = edge;

				neighbors.add(neighbor);
			}
		}

		return neighbors;
	}

	protected ArrayList<Edge> getReversePath(Node root, Node endNode) {
		ArrayList<Edge> path = new ArrayList<Edge>();
		Node node = endNode;
		while (node != root) {
			path.add(node.fromParent);
			node = node.fromParent.source;
		}
		return path;
	}

	private boolean contains(Marking visitedArray[], Marking node) {
		Place[] places = mrpn.getPlaces();

		for (Marking m : visitedArray) {
			boolean flag = false;
			for (Place p : places) {
				ArrayList<Token> mTokens = m.getTokens(p);
				ArrayList<Token> nodeTokens = node.getTokens(p);
				if (mTokens.size() == 0 && nodeTokens.size() == 0)
					continue;
				if (mTokens.size() != nodeTokens.size()) {
					flag = false;
					break;
				}
				for (Token nt : nodeTokens) {
					flag = false;
					for (Token mt : mTokens) {
						if (utils.tokenComparison(nt, mt)) {
							mTokens.remove(mt);
							flag = true;
							break;
						}
					}
					if (!flag)
						break;
				}
				if (!flag)
					break;
			}
			if (flag)
				return true;
		}
		return false;
	}

	public ArrayList<Edge> search(SearchMarking targetMarking) {

		// mrpn marking
		Marking startMarking = mrpn.getMarking();
		Marking mrpnMarking = new Marking(startMarking);

		Node rootNode = new Node(mrpnMarking, 1);

		Queue<Node> queue = new LinkedList<Node>();
		Set<Marking> visited = new HashSet<Marking>();

		// Add initial state
		queue.add(rootNode);

		int counter1 = 0;
		int counter2 = 0;
		long start_time = System.nanoTime();
		long end_time_check;
		long time_check = 0;
		// BFS algorithm
		while (!queue.isEmpty()) {

			// Get next
			Node node = queue.poll();
			counter1++;
			// Check marking
			Marking visitedArray[] = new Marking[visited.size()];
			visited.toArray(visitedArray);
			
			long start_time_check = System.nanoTime();
			if (contains(visitedArray, node.marking)) {
				end_time_check = System.nanoTime();
				time_check += end_time_check - start_time_check;
				continue;
			}
			end_time_check = System.nanoTime();
			time_check += end_time_check - start_time_check;
			counter2++;
			
			//System.out.println(".." + node.marking);

			// Go to marking
			mrpn.setMarking(node.marking);
			
			if (checkTarget(targetMarking, node.marking)) {
				System.out.println("Found path!");
				ArrayList<Edge> path = getReversePath(rootNode, node);
				Collections.reverse(path);
				mrpn.setMarking(startMarking);
				return path;
			}
			
			// Piase tous geitones
			Set<Node> neighbors = getNeighbors(node);
			for (Node neighbor : neighbors) {
				queue.add(neighbor);
			}

			// Episkef8ikame ton kombo
			visited.add(node.marking);

		}
		mrpn.setMarking(startMarking);
		
		long end_time = System.nanoTime();
		long time = end_time - start_time;
		System.out.println("search: " + time/1000000.0f + " ms");
		System.out.println("check: " + time_check/1000000.0f + " ms");
		System.out.println("unique states: " + counter2);
		System.out.println("all states: " + counter1);
		System.out.println("No path found");
		return null;
	}

	private boolean checkTarget(SearchMarking targetMarking, Marking marking) {
		ArrayList<Place> places = targetMarking.getPlaces();
		for (Place p : places) {
			boolean flags[] = new boolean[marking.getTokens(p).size()];
			String tokenMatch[] = new String[marking.getTokens(p).size()];
			for (int i = 0; i < flags.length; i++)
				flags[i] = false;
			if (!checkTarket(targetMarking, marking, p, flags, tokenMatch, 0))
				return false;
		}
		return true;
	}

	private boolean checkTarket(SearchMarking targetMarking, Marking marking, Place p, boolean flags[],
			String tokenMatch[], int i) {
		ArrayList<String> targetTokens = targetMarking.getTokens(p);
		ArrayList<Token> markingTokens = marking.getTokens(p);
		if (targetTokens.size() == i) {
			if(checkBonds(targetMarking,marking,p,flags,tokenMatch))
				return true;
			else
				return false;
		}

		String token = targetTokens.get(i);
		String type = token.substring(0, token.indexOf('('));
		for (int j = 0; j < markingTokens.size(); j++) {
			Token t = markingTokens.get(j);
			if (!flags[j] && t.getType().equals(type)) {
				flags[j] = true;
				tokenMatch[j] = token;
				if (checkTarket(targetMarking, marking, p, flags, tokenMatch, i + 1))
					return true;
				else
					flags[j] = false;
			}
		}
		return false;

	}

	private boolean checkBonds(SearchMarking targetMarking, Marking marking, Place p, boolean flags[],String tokenMatch[]) {
		ArrayList<String> targetTokens = targetMarking.getTokens(p);
		ArrayList<Token> markingTokens = marking.getTokens(p);

		for (String token : targetTokens) {
			int t = -1;
			for (int z = 0; z < tokenMatch.length; z++) 
				if(flags[z] && tokenMatch[z].equals(token)) {
					t = z;
					break;
				}
			
			Set<Pair<String,Boolean>> bonds = targetMarking.getConnectedTokens(token);
			if(bonds == null)
				continue;
			for (Pair<String,Boolean> b : bonds) {
				boolean flag = false;
				String bond = b.getKey();
				boolean noBond = b.getValue(); 
				Set<Token> bondsOfT = markingTokens.get(t).getBonds();
				int numOfBonds = 0;
				if(bondsOfT != null)
					numOfBonds = bondsOfT.size();
				if(noBond && numOfBonds != numOfStrongBonds(bonds))
					return false;
				for (int z = 0; z < tokenMatch.length; z++) {
					if (flags[z] && tokenMatch[z].equals(bond) && mrpn.hasBond(markingTokens.get(t).getName(), markingTokens.get(z).getName())) {
						Set<Pair<String,Boolean>> bondBonds = targetMarking.getConnectedTokens(bond);
						Set<Token> bondsOfZ = markingTokens.get(z).getBonds();
						int numOfBonds2 = 0;
						if(bondsOfZ != null)
							numOfBonds2 = bondsOfZ.size();
						if(noBond && numOfBonds2 != numOfStrongBonds(bondBonds))
							continue;
						flag = true;
						break;
					}
				}
				if(!flag)
					return false;
			}
		}
		return true;
	}

	private int numOfStrongBonds(Set<Pair<String, Boolean>> bonds) {
		int count = 0;
		if(bonds == null)
			return 0;
		for (Pair<String,Boolean> b : bonds) {
			if(b.getValue())
				count++;
		}
		return count;
	}

}
