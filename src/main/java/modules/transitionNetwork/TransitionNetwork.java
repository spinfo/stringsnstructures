package modules.transitionNetwork;

import java.io.IOException;

import modules.OutputPort;
import modules.transitionNetwork.List.TNArrayList;
import modules.transitionNetwork.elements.comparator.StateComparator;
import modules.transitionNetwork.elements.StateElement;
import modules.transitionNetwork.elements.StateTransitionElement;
import modules.transitionNetwork.elements.comparator.SuffixComparator;
import modules.transitionNetwork.elements.SuffixElement;

public class TransitionNetwork {

	public TNArrayList<StateElement> states = null;
	public TNArrayList<SuffixElement> suffixes = null;
	private StateComparator stateComparator;
	private SuffixComparator suffixComparator;
	private char[] text;
	private boolean inverted;

	// A final state id is provided to the user in order to model all
	// transitions to a final state as transitions to the same object
	private final int finalStateId = Integer.MAX_VALUE;
	private final StateElement finalState = new StateElement(finalStateId);

	public TransitionNetwork(char[] text, boolean inverted) {
		this.states = new TNArrayList<StateElement>();
		this.suffixes = new TNArrayList<SuffixElement>();
		this.stateComparator = new StateComparator();
		this.suffixComparator = new SuffixComparator(text);
		this.text = text;
		this.inverted = inverted;
	}

	public int addStateElement(StateElement stateElement) {
		if (this.finalState.equals(stateElement)) {
			return this.finalStateId;
		}
		
		int index = this.states.find(stateElement, this.stateComparator);

		if (index < 0) {
			this.states.add(stateElement);
			index = this.states.size() - 1;
		}
		return index;
	}

	public int addSuffixElement(SuffixElement suffixElement) {
		int index = this.suffixes.find(suffixElement, this.suffixComparator);
		if (index < 0) {
			this.suffixes.add(suffixElement);
			index = this.suffixes.size() - 1;
		}
		return index;
	}

	public int getFinalStateId() {
		return this.finalStateId;
	}

	public void writeSuffixes(OutputPort out) throws IOException {
		out.outputToAllCharPipes("writeSuffixes\n");
		for (int i = 0; i < this.suffixes.size(); i++) {
			SuffixElement e = (SuffixElement) this.suffixes.get(i);
			out.outputToAllCharPipes(e.writeSuffix(text, this.inverted) + "\n");
		}

	}

	public void writeTN(OutputPort out) throws IOException {
		if (out == null) {
			return;
		}

		out.outputToAllCharPipes("writeTN\n");
		this.writeSuffixes(out);
		out.outputToAllCharPipes("writeStates\n");
		for (int i = 0; i < this.states.size(); i++) {
			StateElement stateElement = this.states.get(i);
			out.outputToAllCharPipes("Line: " + i + "  " + "State(Element)(Node): " + stateElement.state + "\n");
			for (int j = 0; j < stateElement.toStateTransitions.size(); j++) {
				StateTransitionElement stateTransitionElement = stateElement.toStateTransitions.get(j);
				out.outputToAllCharPipes(
						"		StateTransitionElement: " + stateTransitionElement.toStateElement + "  ");
				SuffixElement suffixElement = this.suffixes.get(stateTransitionElement.toSuffixElement);
				out.outputToAllCharPipes(suffixElement.writeSuffix(text, this.inverted) + "\n");
			}
		}
	}

	public void writeRows() {
		StringBuilder sb = new StringBuilder();

		// first row: suffixes
		sb.append(",");
		for (SuffixElement suffix : this.suffixes) {
			sb.append(suffix.writeSuffix(text, inverted));
			sb.append(", ");
		}
		sb.setLength(sb.length() - 1);
		sb.append("\n");

		// rows: single states
		StateElement state = null;
		for (int i = 0; i < states.size(); i++) {
			state = states.get(i);
			// do not output final states
			if (finalState.equals(state)) {
				continue;
			}
			// output no of the state
			sb.append("S");
			sb.append(i);
			sb.append(": ");
			// output state transition: suffix to state no
			for (StateTransitionElement st : state.toStateTransitions) {
				sb.append('"');
				sb.append(suffixes.get(st.toSuffixElement).writeSuffix(text, inverted));
				sb.append("\" -> S");
				if (finalStateId == st.toStateElement) {
					sb.append('F');
				} else {
					sb.append(st.toStateElement);
				}
				sb.append(", ");
			}
			if (state.toStateTransitions.size() > 0) {
				sb.setLength(sb.length() - 2);
			}
			sb.append("\n");
		}

		System.out.println(sb.toString());
	}

}
