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
	private String name = "";
	private Action lastAction = Action.DO_NOTHING;

	private Properties props;
	private StanfordCoreNLP pipeline;

	private Scanner sc;
	private String OriginalString = "";

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
		OriginalString = name.toLowerCase();
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
			System.out.print(graph.toString());
			IndexedWord root = graph.getFirstRoot();
			String type = root.tag();
			switch (type) {
			case "JJ":
				result = processAdjective(graph, root);
				lastAction = result;
				break;
			case "VB":
				result = processVerb(graph, root);
				lastAction = result;
				break;
			case "RB":
				result = processAdverb(graph, root);
				lastAction = result;
				break;
			case "UH":
				result = processInterjection(graph, root);
				lastAction = result;
				break;
			case "JJR":
				result = processComparativeJ(graph, root);
				break;
			default:
				result = doNotUnderstand();
				lastAction = result;
			}

		}
		return result;
	}

	private Action processComparativeJ(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		String first = root.originalText().toLowerCase();
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			if (!p.first.toString().equals("appos") && !p.first.toString().equals("punct")
					&& !p.first.toString().equals("npmod") && !p.first.toString().equals("nmod:poss")
					&& !p.first.toString().equals("dep") && !p.first.toString().equals("advmod")) {
				return doNotUnderstand();
			}
			if (p.first.toString().equals("advmod")) {
				if (p.second.tag().equals("RP")) {
					if (p.second.originalText().toLowerCase().equals("up")) {
						if (lastAction != Action.MOVE_UP) {
							return doNotUnderstand();
						} else {
							return lastAction;
						}
					} else if (p.second.originalText().toLowerCase().equals("down")) {
						if (lastAction != Action.MOVE_DOWN) {
							return doNotUnderstand();
						} else {
							return lastAction;
						}
					}
				}
			}
		}

		return Action.DO_NOTHING;
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
						return Action.CLEAN;
					}
				}
			}
		} else if (first.equals("right")) {
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (!p.first.toString().equals("appos") && !p.first.toString().equals("punct")
						&& !p.first.toString().equals("npmod") && !p.first.toString().equals("nmod:poss")
						&& !p.first.toString().equals("dep") && !p.first.toString().equals("advmod")) {
					re = doNotUnderstand();
					return re;
				}
				if (p.first.toString().equals("advmod")) {
					if (p.second.tag().equals("RBR")) {
						if (lastAction != Action.MOVE_RIGHT) {
							re = doNotUnderstand();
							return re;
						} else {
							re = lastAction;
						}
					}
				}
			}
		} else if (first.equals("left")) {
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (!p.first.toString().equals("appos") && !p.first.toString().equals("punct")
						&& !p.first.toString().equals("npmod") && !p.first.toString().equals("nmod:poss")
						&& !p.first.toString().equals("dep") && !p.first.toString().equals("advmod")) {
					re = doNotUnderstand();
					return re;
				}
				if (p.first.toString().equals("advmod")) {
					if (p.second.tag().equals("JJR")) {
						if (lastAction != Action.MOVE_LEFT) {
							re = doNotUnderstand();
							return re;
						} else {
							re = lastAction;
						}
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
		if (first.equals("move")) {
			boolean hasConfirmed = false;
			for (Pair<GrammaticalRelation, IndexedWord> pair : s) {
				if (pair.first().toString().equals("advmod")) {
					Pair<GrammaticalRelation, IndexedWord> adverb = pair;
					if (adverb.second.tag().equals("RB")) {
						String str = adverb.second.originalText();
						switch (str) {
						case "right":
							if (hasConfirmed && re != Action.MOVE_RIGHT) {
								// Do not understand rely
								re = doNotUnderstand();
								return re;
							}
							re = Action.MOVE_RIGHT;
							hasConfirmed = true;
							break;
						case "left":
							if (hasConfirmed && re != Action.MOVE_LEFT) {
								// Do not understand rely
								re = doNotUnderstand();
								return re;
							}
							re = Action.MOVE_LEFT;
							hasConfirmed = true;
							break;
						case "up":
							if (hasConfirmed && re != Action.MOVE_UP) {
								// Do not understand rely
								re = doNotUnderstand();
								return re;
							}
							re = Action.MOVE_UP;
							hasConfirmed = true;
							break;
						case "down":
							if (hasConfirmed && re != Action.MOVE_DOWN) {
								// Do not understand rely
								re = doNotUnderstand();
								return re;
							}
							re = Action.MOVE_DOWN;
							hasConfirmed = true;
							break;
						default:
							break;
						}
					}
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
			if (first.equals("again")) {
				for (Pair<GrammaticalRelation, IndexedWord> p : s) {
					if (!p.first.toString().equals("appos") && !p.first.toString().equals("punct")
							&& !p.first.toString().equals("npmod") && !p.first.toString().equals("dep")) {
						lastAction = doNotUnderstand();
						return lastAction;
					}
				}
				if (lastAction == Action.DO_NOTHING) {
					System.out.println("Sorry! Your last command was not clear!");
				}
				re = lastAction;
			}
			return re;
		}
		switch (first) {
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
		case "again":
			if (lastAction == Action.DO_NOTHING) {
				System.out.println("Sorry! Your last command was not clear!");
			}
			re = lastAction;
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

	private Action doNotUnderstand() {
		Action re = keyWordSearch();
		if (re == Action.DO_NOTHING) {
			double seed = Math.random() * 5;
			if (seed <= 1) {
				System.out.println("Sorry, I didn't understand what you have said!");
			} else if (seed <= 2) {
				System.out.println("Oops. I didn't get that!");
			} else if (seed <= 3) {
				System.out.println("Sorry! I don't know that!");
			} else if (seed <= 4) {
				System.out.println("Sorry! Please say that again!");
			} else if (seed < 4.99) {
				System.out.println("I'm sorry! I didn't catch that!");
			} else {
				System.out.println("What did you say?");
			}
		}
		return re;
	}

	private Action keyWordSearch() {
		if (OriginalString.contains("right")) {
			randomPrintIThink("right");
			return Action.MOVE_RIGHT;
		} else if (OriginalString.contains("left")) {
			return Action.MOVE_LEFT;
		} else if (OriginalString.contains("up")) {
			return Action.MOVE_UP;
		} else if (OriginalString.contains("down")) {
			return Action.MOVE_DOWN;
		} else if (OriginalString.contains("clean")) {
			return Action.CLEAN;
		}
		return Action.DO_NOTHING;
	}

	private void randomPrintIThink(String string) {
		double seed = Math.random() * 5;
		System.out.print("I think you want to");
		if (seed <= 1) {
			if(string.equals("clean")) {
				System.out.println("clean!");
				return;
			}
			System.out.println("move " + string + "!");
		} else if (seed <= 2) {
			if(string.equals("clean")) {
				System.out.println("do some cleaning!");
				return;
			}
			System.out.println("turn " + string + "!");
		} else if (seed <= 3) {
			if(string.equals("clean")) {
				System.out.println("let me clean this place!");
				return;
			}
			System.out.println("go " + string + "!");
		} else if (seed <= 4) {
			if(string.equals("clean")) {
				System.out.println("rinse this place!");
				return;
			}
			System.out.println("let me jump " + string + "!");
		} else {
			if(string.equals("clean")) {
				System.out.println("wipe this!");
				return;
			}
			System.out.println("let me go " + string + "!");
		}
	}

}