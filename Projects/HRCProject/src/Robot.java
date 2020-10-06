import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;

	private Properties props;
	private StanfordCoreNLP pipeline;

	private Scanner sc;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(props);

	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
//	    System.out.println("got: " + name);
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		Action result = Action.DO_NOTHING;
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			// TODO: remove prettyPrint() and use the SemanticGraph to determine the action
			// to be executed by the robot.
			IndexedWord root = graph.getFirstRoot();

			String type = root.tag();
			switch (type) {
			case "JJ":
				result = processAdjective(graph, root);
				break;
			case "VB":
				result = processVerb(graph, root);
				break;
			case "RB":
				result = processAdverb(graph, root);
				break;
			case "UH":
				result = processInterjection(graph, root);
				break;
			default:
				System.out.print("Cannot identify sentence structure.");
			}
		}

		return result;
	}

	private Action processAdjective(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		Action re = Action.DO_NOTHING;
		String first = root.originalText().toLowerCase();
		if (first.equals("clean")) {
			System.out.println(s.size());
			if (s.size() == 0) {
				return Action.CLEAN;
			}
			if (s.size() == 1) {
				Pair<GrammaticalRelation, IndexedWord> adverb = s.get(0);
				System.out.println(adverb.second.tag());
				if (adverb.second.tag().equals("UH")) {
					String str = adverb.second.originalText();
					if (str.equals("please")) {
						System.out.println(str);
						return Action.CLEAN;
					}
				}
			}
		}
		return re;
	}

	private Action processVerb(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		Action re = Action.DO_NOTHING;
		String first = root.originalText().toLowerCase();
		Pair<GrammaticalRelation, IndexedWord> adverb = s.get(0);
		if (first.equals("move")) {
			for(Pair<GrammaticalRelation, IndexedWord> pair: s) {
				if(pair.second.tag().equals("RB")) {
					adverb = pair;
				}
			}
			if (adverb.second.tag().equals("RB")) {
				String str = adverb.second.originalText();
				switch (str) {
				case "right":
					re = Action.MOVE_RIGHT;
					break;
				case "left":
					re = Action.MOVE_LEFT;
					break;
				case "up":
					re = Action.MOVE_UP;
					break;
				case "down":
					re = Action.MOVE_DOWN;
					break;
				default:
					break;
				}
			}
		} else if (first.equals("clean")) {
			if (s.size() == 1) {
				return Action.CLEAN;
			}
		}
		return re;
	}

	private Action processAdverb(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		Action re = Action.DO_NOTHING;
		String first = root.originalText().toLowerCase();
		if (s.size() > 1) {
			return re;
		}
		switch (first) {
		case "left":
			re = Action.MOVE_LEFT;
			break;
		case "up":
			re = Action.MOVE_UP;
			break;
		case "down":
			re = Action.MOVE_DOWN;
			break;
		default:
			break;
		}
		return re;
	}

	private Action processInterjection(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);

		String first = root.originalText().toLowerCase();

		if (first.equals("right")) {
			return Action.MOVE_RIGHT;
		}
		return Action.DO_NOTHING;
	}

}