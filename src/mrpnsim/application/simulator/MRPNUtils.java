package mrpnsim.application.simulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javafx.util.Pair;
import mrpnsim.application.model.Arrow;
import mrpnsim.application.model.LabelItem;
import mrpnsim.application.model.Place;
import mrpnsim.application.model.MRPN;
import mrpnsim.application.model.Token;
import mrpnsim.application.model.Transition;

public class MRPNUtils {

	private MRPN mrpn;
	public MRPNUtils( MRPN mrpn ) {
		this.mrpn = mrpn;
	}
	
	public Set<LabelItem> getBondsFrom( Set<LabelItem> labelItems ){
		Set<LabelItem> bonds = new HashSet<LabelItem>();
		
		for( LabelItem item : labelItems ) {
			if( item.isBond()) {
				bonds.add( item );
			}
		}
		
		return bonds;
	}
	
	public Set<LabelItem> F(Transition transition, Place place){
		Set<LabelItem> set = new HashSet<LabelItem>();
		
		// Find arrow that connects to place
		for( Arrow arrow : transition.getArrows() ) {
			if( arrow.getDestination() == place ) {
				set.addAll( arrow.getSet() );
				break;
			}
		}
		
		return set;
	}
	
	public ArrayList< Set<LabelItem> > outLabels(Transition transition){
		ArrayList< Set<LabelItem> > arraySet = new ArrayList<Set<LabelItem>>();
		// Find arrow that connects to place
		for( Arrow arrow : transition.getArrows() ) {
			if( arrow.getSource() == transition ) 
				arraySet.add(  arrow.getSet() );
		}
		return arraySet;
	}	

	protected Set<LabelItem> expandSet( Set<LabelItem> set ){
		Set<LabelItem> expandedSet = new HashSet<LabelItem>();
		for( LabelItem item : set ) {
			
			if( item.isBond()) {
				Pair<String,String> bond = item.getBond();
				LabelItem newItemA = new LabelItem(bond.getKey());
				LabelItem newItemB = new LabelItem(bond.getValue());				
				expandedSet.add(newItemA);
				expandedSet.add(newItemB);
			}
			expandedSet.add(item);
		}
		
		return expandedSet;
	}
	
	
	
	public Set<LabelItem> intersection( Set<LabelItem> setA, Set<LabelItem> setB ) {
		Set<LabelItem> setC = new HashSet<LabelItem>();
		
		Set<LabelItem> expandedSetA = expandSet(setA);
		Set<LabelItem> expandedSetB = expandSet(setB);
		
		for( LabelItem itemA : expandedSetA ) {
			for( LabelItem itemB : expandedSetB ) {
				
				if( itemA.equals(itemB) )
					setC.add(itemA);
				
			}
		}
		
		return setC;
	}
	
	public boolean subsetOf( Set<LabelItem> setA, Set<LabelItem> setB ) {
		
		for( LabelItem itemA : setA ) {
			boolean contains = false;
			
			for(LabelItem itemB : setB ) {
				if( itemA.equals(itemB) ) {
					contains = true;
					break;
				}
			}
			if(contains == false)
				return false;
		}
		return true;
	}
	
	public Set<String> getTokensFrom( Set<LabelItem> labelItems ){
		Set<String> tokens = new HashSet<String>();
		
		for( LabelItem item : labelItems ) {
			
			if( item.isToken()) {
				String token = item.getTokenName();
				tokens.add( token );
			}
			
			// If bond a-b, then it's assumed that we also have a,b tokens
			if( item.isBond()) {
				Pair<String,String> bond = item.getBond();
				String tokenA = bond.getKey();
				tokens.add( tokenA );
				
				String tokenB = bond.getValue();
				tokens.add( tokenB );
			}
		}
		
		return tokens;
	}
	
	public boolean compare( Set<String> setA, Set<String> setB ) {
		return setB.containsAll( setA ) && setA.containsAll( setB );
	}
	
	public boolean compareLabelItems( Set<LabelItem> setA, Set<LabelItem> setB ) {
		return setB.containsAll( setA ) && setA.containsAll( setB );
	}
	
	
	public Set<LabelItem> pre(Transition transition){
		Set<LabelItem> set = new HashSet<LabelItem>();
		
		// Find incoming arrows
		Arrow[] arrows = transition.getArrows();
		for(Arrow arrow : arrows ) {
			
			if( arrow.getDestination() == transition ) {
				set.addAll(  arrow.getSet() ); // Union
			}
		}
		return set;
	}
	
	
	public Set<LabelItem> post(Transition transition){
		Set<LabelItem> set = new HashSet<LabelItem>();
		
		// Find outcoming arrows
		Arrow[] arrows = transition.getArrows();
		for(Arrow arrow : arrows ) {
			
			if( arrow.getSource() == transition ) {
				set.addAll(  arrow.getSet() ); // Union		
			}
		}
		return set;
	}
	
	private boolean tokenComparison(Token A, Token B, ArrayList<Token> visitedA, ArrayList<Token> visitedB) {
		Set<Token> setA = A.getBonds();
		Set<Token> setB = B.getBonds();
		if ((setA == null || setB == null))
			return (setA == null && setB == null);
		if (setA.size() != setB.size())
			return false;

		Token[] bondsA = new Token[setA.size()];
		setA.toArray(bondsA);
		Token[] bondsB = new Token[setB.size()];
		setB.toArray(bondsB);
		boolean[] flag = new boolean[setB.size()];
		for (int i = 0; i < setB.size(); i++)
			flag[i] = false;

		for (int i = 0; i < setA.size(); i++)
			for (int j = 0; j < setB.size(); j++)
				if (!flag[j] && bondsA[i].getType().equals(bondsB[j].getType())) {
					flag[j] = true;
					break;
				}
		for (int i = 0; i < setB.size(); i++)
			if (!flag[i])
				return false;

		for (int i = 0; i < setB.size(); i++)
			flag[i] = false;
		for (int i = 0; i < setA.size(); i++)
			for (int j = 0; j < setB.size(); j++)
				if (!flag[j] && bondsA[i].getType().equals(bondsB[j].getType())) {
					if (visitedA.contains((bondsA[i])) && visitedB.contains((bondsB[j]))) {
						flag[j] = true;
						break;
					}
					if (visitedA.contains((bondsA[i])) || visitedB.contains((bondsB[j])))
						continue;
					visitedA.add(A);
					visitedB.add(B);
					if (tokenComparison(bondsA[i], bondsB[j], visitedA, visitedB)) {
						flag[j] = true;
						break;
					} else {
						visitedA.remove(A);
						visitedB.remove(B);
					}
				}

		for (int i = 0; i < setB.size(); i++)
			if (!flag[i])
				return false;
		return true;
	}

	public boolean tokenComparison(Token A, Token B) {
		if (!A.getType().equals(B.getType()))
			return false;
		Set<Token> setA = A.getBonds();
		Set<Token> setB = B.getBonds();
		if ((setA == null || setB == null))
			return (setA == null && setB == null);
		if (setA.size() != setB.size())
			return false;

		Token[] bondsA = new Token[setA.size()];
		setA.toArray(bondsA);
		Token[] bondsB = new Token[setB.size()];
		setB.toArray(bondsB);
		ArrayList<Token> visitedA = new ArrayList<>();
		ArrayList<Token> visitedB = new ArrayList<>();
		boolean[] flag = new boolean[setB.size()];
		for (int i = 0; i < setB.size(); i++)
			flag[i] = false;

		for (int i = 0; i < setA.size(); i++)
			for (int j = 0; j < setB.size(); j++)
				if (!flag[j] && bondsA[i].getType().equals(bondsB[j].getType())) {
					flag[j] = true;
					break;
				}
		for (int i = 0; i < setB.size(); i++)
			if (!flag[i])
				return false;
		for (int i = 0; i < setB.size(); i++)
			flag[i] = false;
		for (int i = 0; i < setA.size(); i++)
			for (int j = 0; j < setB.size(); j++)
				if (!flag[j] && bondsA[i].getType().equals(bondsB[j].getType())) {
					visitedA.add(A);
					visitedB.add(B);
					if (tokenComparison(bondsA[i], bondsB[j], visitedA, visitedB)) {
						flag[j] = true;
						break;
					} else {
						visitedA.remove(A);
						visitedB.remove(B);
					}

				}

		for (int i = 0; i < setB.size(); i++)
			if (!flag[i])
				return false;
		return true;
	}
}
