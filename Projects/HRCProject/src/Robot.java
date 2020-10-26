import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

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
	private String userName = "";
	private Action lastAction = Action.DO_NOTHING;
	private Stack<Action> stack;
	private HashMap<String, Plan> plans;

	private Properties props;
	private StanfordCoreNLP pipeline;

	private Scanner sc;
	private String OriginalString = "";
	private boolean giveName = false;
	private boolean takeName = false;
	private boolean recording = false;
	private boolean planName = false;
	private Plan recordingPlan = null;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.stack = new Stack<Action>();
		this.plans = new HashMap<String, Plan>();
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
		if (stack.isEmpty()) {
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
			if (name.equals("begin record") || name.equals("start record")) {
				recording = true;
				this.recordingPlan = new Plan("");
				respond();
				return Action.DO_NOTHING;
			}
			if (name.equals("end record") || name.equals("stop record")) {
				recording = false;
				planName = true;
				System.out.println("Please name the plan!");
				return Action.DO_NOTHING;
			}
			if (sentences != null && !sentences.isEmpty()) {
				CoreMap sentence = sentences.get(0);
				SemanticGraph graph = sentence
						.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

				System.out.print(graph.toString());
				IndexedWord root = graph.getFirstRoot();
				String type = root.tag();
				if (planName) {
					String str = processPlanName(graph, root);
					if (str != null) {
						this.recordingPlan.setName(str);
						this.plans.put(str, this.recordingPlan);
					}
					this.recordingPlan = null;
					this.recording = false;
					this.planName = false;
					return Action.DO_NOTHING;
				}
				result = getActionFromASentence(graph, root);
				stack.push(result);
			}
		}
		Action result = stack.pop();
		if (recording) {
			this.recordingPlan.addAction(result);
		}
		return result;
	}

	private String processPlanName(SemanticGraph graph, IndexedWord root) {
		if (root.tag().equals("VB")) {
			if (root.originalText().toLowerCase().equals("name")) {
				List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
				for (Pair<GrammaticalRelation, IndexedWord> p : s) {
					if (p.second.tag().equals("NN")) {
						if (!p.second.originalText().toLowerCase().equals("plan")) {
							System.out.println("I cannot understand Plan name. Abort!");
							return null;
						}
					} else if (p.second.tag().equals("NNP")) {
						System.out.println("Add New Plan " + p.second.originalText());
						return p.second.originalText();
					}
				}
			}
		}
		System.out.println("I cannot understand Plan name. Abort!");
		return null;
	}

	private Action getActionFromASentence(SemanticGraph graph, IndexedWord root) {
		String type = root.tag();
		Action result = Action.DO_NOTHING;
		if (!giveName && !takeName) {
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
			case "WP":
				result = processWhpronoun(graph, root);
				break;
			case "NN":
				result = processNoun(graph, root);
				break;
			case "NNP":
				result = processNNP(graph, root);
				break;
			default:
				result = doNotUnderstand();
				lastAction = result;
			}
		} else if (giveName) {
			if (type.equals("NNP")) {
				List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
				for (Pair<GrammaticalRelation, IndexedWord> p : s) {
					if (p.first.toString().toLowerCase().equals("nsubj")) {
						if (p.second.originalText().toLowerCase().equals("name")) {
							for (Pair<GrammaticalRelation, IndexedWord> p1 : graph.childPairs(p.second)) {
								if (p1.first.toString().equals("nmod:poss")) {
									if (!p1.second.originalText().toLowerCase().equals("your")) {
										giveName = false;
										return doNotUnderstand();
									}
								}
							}
							this.name = root.originalText();
							System.out.println("Thank you! I will be " + this.name + ".");
							giveName = false;
							return Action.DO_NOTHING;
						} else if (p.second.originalText().toLowerCase().equals("you")) {
							this.name = root.originalText();
							System.out.println("Thank you! I will be " + this.name + ".");
							giveName = false;
							return Action.DO_NOTHING;
						}
					}
				}
				this.name = root.originalText();
				System.out.println("Thank you! I will be " + this.name + ".");
				giveName = false;
				return Action.DO_NOTHING;
			}
			giveName = false;
		} else {
			if (type.equals("NNP")) {
				List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
				for (Pair<GrammaticalRelation, IndexedWord> p : s) {
					if (p.first.toString().toLowerCase().equals("nsubj")) {
						if (p.second.originalText().toLowerCase().equals("name")) {
							for (Pair<GrammaticalRelation, IndexedWord> p1 : graph.childPairs(p.second)) {
								if (p1.first.toString().equals("nmod:poss")) {
									if (!p1.second.originalText().toLowerCase().equals("my")) {
										takeName = false;
										return doNotUnderstand();
									}
								}
							}
							this.userName = root.originalText();
							System.out.println("Thank you, " + this.userName + ".");
							takeName = false;
							return Action.DO_NOTHING;
						} else if (p.second.originalText().toLowerCase().equals("i")) {
							this.userName = root.originalText();
							System.out.println("Thank you, " + this.userName + ".");
							takeName = false;
							return Action.DO_NOTHING;
						}
					}
				}
				this.userName = root.originalText();
				System.out.println("Thank you, " + this.userName + ".");
				takeName = false;
				return Action.DO_NOTHING;
			}
			takeName = false;
		}
		return result;
	}

	private Action processNNP(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		String first = root.originalText().toLowerCase();
		if (first.equals("random")) {
			System.out.println("Is this OK?");
			double seed = Math.random() * 4;
			if (seed <= 1) {
				return Action.MOVE_RIGHT;
			} else if (seed <= 2) {
				return Action.MOVE_LEFT;
			} else if (seed <= 3) {
				return Action.MOVE_UP;
			} else {
				return Action.MOVE_DOWN;
			}
		}
		return doNotUnderstand();
	}

	private Action processNoun(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		String first = root.originalText().toLowerCase();
		if (first.equals("job")) {
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.first.toString().equals("amod")) {
					if (p.second.originalText().toLowerCase().equals("good")
							|| p.second.originalText().toLowerCase().equals("Nice")) {
						System.out.println("My pleasure to serve!");
						return Action.DO_NOTHING;
					}
				}
			}
		}
		return doNotUnderstand();
	}

	private Action processWhpronoun(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		String first = root.originalText().toLowerCase();
		if (first.equals("what")) {
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.first.toString().equals("nsubj")) {
					if (p.second.originalText().equals("name")) {
						for (Pair<GrammaticalRelation, IndexedWord> p1 : graph.childPairs(p.second)) {
							if (p1.first.toString().equals("nmod:poss")) {
								if (p1.second.originalText().equals("your")) {
									if (name.equals("")) {
										System.out.println("I don't have a name. Can you give me one?");
										giveName = true;
										return Action.DO_NOTHING;
									} else {
										System.out.println("I am " + name);
										return Action.DO_NOTHING;
									}
								} else if (p1.second.originalText().equals("my")) {
									if (userName.equals("")) {
										System.out.println("I don't know. Can you tell me?");
										takeName = true;
										return Action.DO_NOTHING;
									} else {
										System.out.println("You are " + userName);
										return Action.DO_NOTHING;
									}
								} else {
									doNotUnderstand();
								}
							}
						}
					}
				}
			}
			doNotUnderstand();
		}
		return Action.DO_NOTHING;
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
		respond();
		return Action.DO_NOTHING;
	}

	private Action processAdjective(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);
		Action re = Action.DO_NOTHING;
		String first = root.originalText().toLowerCase();
		if (first.equals("clean")) {
			if (s.size() == 0) {
				respond();
				return Action.CLEAN;
			}
			if (s.size() == 1) {
				Pair<GrammaticalRelation, IndexedWord> adverb = s.get(0);
				System.out.println(adverb.second.tag());
				if (adverb.second.tag().equals("UH")) {
					String str = adverb.second.originalText();
					if (str.equals("please")) {
						respond();
						return Action.CLEAN;
					}
				}
			}
			return doNotUnderstand();
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
			respond();
			return re;
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
			respond();
			return re;
		} else if (first.equals("good") || first.equals("helpful") || first.equals("nice")) {
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.first.toString().equals("nsubj")) {
					if (!p.second.originalText().toLowerCase().equals("you")
							&& !p.second.originalText().toLowerCase().equals(this.name)) {
						return doNotUnderstand();
					}
				}
			}
			System.out.println("Thank you very much!");
			return re;
		}
		return doNotUnderstand();
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
						String str = adverb.second.originalText().toLowerCase();
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
						case "not":
							respond();
							return Action.DO_NOTHING;
						default:
							return doNotUnderstand();
						}
					}
				}
			}
			return doNotUnderstand();
		} else if (first.equals("clean")) {
			if (s.size() == 1) {
				return Action.CLEAN;
			}
			respond();
			return re;
		} else if (first.equals("do")) {
			boolean again = false;
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (!p.first.toString().equals("discourse") && !p.first.toString().equals("punct")
						&& !p.first.toString().equals("npmod") && !p.first.toString().equals("dep")
						&& !p.first.toString().equals("advmod") && !p.first.toString().equals("obj")
						&& !p.first.toString().equals("xcomp")) {
					lastAction = doNotUnderstand();
					return lastAction;
				}
				if (p.first.toString().equals("advmod") || p.first.toString().equals("xcomp")) {
					if (p.second.originalText().equals("again")) {
						again = true;
					}
					if (p.second.originalText().equals("not")) {
						return Action.DO_NOTHING;
					}
				}
			}
			if (again) {
				if (lastAction == Action.DO_NOTHING) {
					System.out.println("Sorry! Your last command was not clear!");
					return doNotUnderstand();
				}
				re = lastAction;
			}
			respond();
			return re;
		} else if (first.equals("undo")) {
			switch (lastAction) {
			case MOVE_RIGHT:
				re = Action.MOVE_LEFT;
				break;
			case MOVE_LEFT:
				re = Action.MOVE_RIGHT;
				break;
			case MOVE_DOWN:
				re = Action.MOVE_UP;
				break;
			case MOVE_UP:
				re = Action.MOVE_DOWN;
				break;
			default:
				System.out.println("It seems to have no action that can be undoed");
				return Action.DO_NOTHING;
			}
			respond();
			return re;
		} else if (first.equals("execute")) {
			boolean sym = false;
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.second().tag().equals("NNP")) {
					if (sym) {
						if (plans.containsKey(p.second.originalText())) {
							Stack<Action> actions = plans.get(p.second.originalText()).getActions();
							while (!actions.isEmpty()) {
								Action act = actions.pop();
								switch (act) {
								case MOVE_RIGHT:
									stack.push(Action.MOVE_LEFT);
									break;
								case MOVE_LEFT:
									stack.push(Action.MOVE_RIGHT);
									break;
								case MOVE_UP:
									stack.push(Action.MOVE_DOWN);
									break;
								case MOVE_DOWN:
									stack.push(Action.MOVE_UP);
									break;
								default:
									stack.push(act);
								}

							}
							respond();
							return Action.DO_NOTHING;
						}
						System.out.println("Cannot find plan " + p.second.originalText());
						return Action.DO_NOTHING;
					}
					List<Pair<GrammaticalRelation, IndexedWord>> s1 = graph.childPairs(p.second());
					for (Pair<GrammaticalRelation, IndexedWord> p2 : s1) {
						if (p2.second().tag().equals("NN")) {
							if (p2.second().originalText().equals("plan")) {
								if (plans.containsKey(p.second.originalText())) {
									Stack<Action> actions = plans.get(p.second.originalText()).getActions();
									while (!actions.isEmpty()) {
										stack.push(actions.pop());
									}
									respond();
									return Action.DO_NOTHING;
								}
								System.out.println("Cannot find plan " + p.second.originalText());
								return Action.DO_NOTHING;
							}
						}
					}
				}
				if (p.second().tag().equals("NN")) {
					if (p.second.originalText().toLowerCase().equals("plan")) {
						List<Pair<GrammaticalRelation, IndexedWord>> s1 = graph.childPairs(p.second());
						for (Pair<GrammaticalRelation, IndexedWord> p2 : s1) {
							if (p2.second().tag().equals("JJ")) {
								if (p2.second().originalText().equals("symmetric")) {
									sym = true;
								}
							}
						}
					}
				}
			}
			re = doNotUnderstand();
			return re;
		} else if (first.equals("find")) {
			int row = posRow;
			int col = posCol;
			Pair<GrammaticalRelation, IndexedWord> p = s.get(0);
			if (p.second.originalText().toLowerCase().equals("path")) {
				Pair<GrammaticalRelation, IndexedWord> p2 = s.get(1);
				if (p2.second.originalText().toLowerCase().equals("row")) {
					List<Pair<GrammaticalRelation, IndexedWord>> s1 = graph.childPairs(p2.second());
					for(Pair<GrammaticalRelation, IndexedWord> p3: s1) {
						if(p3.first.toString().equals("nummod")) {
							row = Integer.parseInt(p3.second.originalText());
						} else if(p3.first.toString().equals("conj:and") && p3.second.originalText().toLowerCase().equals("column")) {
							for(Pair<GrammaticalRelation, IndexedWord> p4: graph.childPairs(p3.second())) {
								if(p4.first.toString().equals("nummod")) {
									col = Integer.parseInt(p4.second.originalText());
									findLocation(row, col);
									this.recording = true;
									this.planName = true;
									this.recordingPlan = new Plan("");
									System.out.println("Please name this plan");
									return Action.DO_NOTHING;
								}
							}
						}
					}
				}
			}
		} else if(first.equals("combine")) {
			ArrayList<String> arr = new ArrayList<String>();
			combineHelper(graph, root, s, arr);
			System.out.println(arr.toString());
		}
		return doNotUnderstand();
	}
	
	private void combineHelper(SemanticGraph graph, IndexedWord root, List<Pair<GrammaticalRelation, IndexedWord>> s, ArrayList<String> arr) {
		if(s.isEmpty()) {
			return;
		} 
		for(Pair<GrammaticalRelation, IndexedWord> p: s) {
			if(p.second.originalText().toLowerCase().equals("plan")) {
				arr.add(root.originalText());
			} 
			if(p.second.tag().equals("NNP")) {
				combineHelper(graph, p.second, graph.childPairs(p.second), arr);
				break;
			}
		}
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
			respond();
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
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.first.toString().equals("advmod") && p.second.originalText().equals("not")) {
					return Action.DO_NOTHING;
				}
			}
			break;
		case "down":
			re = Action.MOVE_DOWN;
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				if (p.first.toString().equals("advmod") && p.second.originalText().equals("not")) {
					return Action.DO_NOTHING;
				}
			}
			break;
		case "again":
			if (lastAction == Action.DO_NOTHING) {
				System.out.println("Sorry! Your last command was not clear!");
			}
			re = lastAction;
			break;
		default:
			return doNotUnderstand();
		}
		respond();
		return re;
	}

	private Action processInterjection(SemanticGraph graph, IndexedWord root) {
		List<Pair<GrammaticalRelation, IndexedWord>> s = graph.childPairs(root);

		String first = root.originalText().toLowerCase();

		if (first.equals("right")) {
			respond();
			return Action.MOVE_RIGHT;
		}
		doNotUnderstand();
		return Action.DO_NOTHING;
	}

	private Action doNotUnderstand() {
		Action re = keyWordSearch();
		if (re == Action.DO_NOTHING) {
			double seed = Math.random() * 10;
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
			} else if (seed < 5) {
				System.out.println("What did you say?");
			} else if (seed < 6) {
				System.out.println("Sorry but I don’t quite follow you.");
			} else if (seed < 7) {
				System.out.println("Could you say it in another way?");
			} else if (seed < 8) {
				System.out.println("Can you clarify that for me?");
			} else if (seed < 9) {
				System.out.println("Could you rephrase that?");
			} else {
				System.out.println("Could you elaborate on that more specifically?");
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
		System.out.print("I think you want to ");
		if (seed <= 1) {
			if (string.equals("clean")) {
				System.out.println("clean!");
				return;
			}
			System.out.println("move " + string + "!");
		} else if (seed <= 2) {
			if (string.equals("clean")) {
				System.out.println("do some cleaning!");
				return;
			}
			System.out.println("turn " + string + "!");
		} else if (seed <= 3) {
			if (string.equals("clean")) {
				System.out.println("let me clean this place!");
				return;
			}
			System.out.println("go " + string + "!");
		} else if (seed <= 4) {
			if (string.equals("clean")) {
				System.out.println("rinse this place!");
				return;
			}
			System.out.println("let me jump " + string + "!");
		} else {
			if (string.equals("clean")) {
				System.out.println("wipe this!");
				return;
			}
			System.out.println("let me go " + string + "!");
		}
	}

	private void respond() {
		double seed = Math.random() * 5;
		if (seed <= 1) {
			System.out.println("Got you!");
		} else if (seed <= 2) {
			System.out.println("OK!");
		} else if (seed <= 3) {
			System.out.println("My pleasure!");
		} else if (seed <= 4) {
			System.out.println("Roger that!");
		} else {
			System.out.println("On my way!");
		}

	}

	private void findLocation(int row, int col) {
		LinkedList<Position> queue = new LinkedList<Position>();
		boolean[][] hasVisited = new boolean[env.getRows()][env.getCols()];
		Action[][] moves = new Action[env.getRows()][env.getCols()];
		for (int i = 0; i < hasVisited.length; i++) {
			for (int j = 0; j < hasVisited[0].length; j++) {
				hasVisited[i][j] = false;
				moves[i][j] = Action.DO_NOTHING;
			}
		}
		Position root = new Position(posRow, posCol);
		boolean targetFound = false;
		queue.add(root);
		hasVisited[root.row][root.col] = true;
		while (!queue.isEmpty()) {
			Position current = queue.poll();
			// System.out.println(current.row + " " + current.col);
			if (env.validPos(current.row, current.col + 1)) {
				Position next = new Position(current.row, current.col + 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col + 1] = Action.MOVE_RIGHT;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row, current.col - 1)) {
				Position next = new Position(current.row, current.col - 1);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row][current.col - 1] = Action.MOVE_LEFT;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row + 1, current.col)) {
				Position next = new Position(current.row + 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row + 1][current.col] = Action.MOVE_DOWN;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
			if (env.validPos(current.row - 1, current.col)) {
				Position next = new Position(current.row - 1, current.col);
				if (!hasVisited[next.row][next.col]) {
					hasVisited[next.row][next.col] = true;
					moves[current.row - 1][current.col] = Action.MOVE_UP;
					if (next.row == row && next.col == col) {
						targetFound = true;
						break;
					}
					queue.add(next);
				}
			}
		}
		if (targetFound) {
			LinkedList<Action> thisTurn = new LinkedList<Action>();
			while (!moves[row][col].equals(Action.DO_NOTHING)) {
				thisTurn.addFirst(moves[row][col]);
				if (moves[row][col].equals(Action.MOVE_RIGHT))
					col--;
				else if (moves[row][col].equals(Action.MOVE_LEFT))
					col++;
				else if (moves[row][col].equals(Action.MOVE_UP))
					row++;
				else
					row--;
			}
			for (int i = 0; i < hasVisited.length; i++) {
				for (int j = 0; j < hasVisited[0].length; j++) {
					hasVisited[i][j] = false;
					moves[i][j] = Action.DO_NOTHING;
				}
			}
			queue.clear();
			stack.addAll(thisTurn);
		} else {
			// Right now; Not possible
		}

	}

}

class Plan {
	private String name;
	private Stack<Action> actions;

	public Plan(String name) {
		this.name = name;
		this.actions = new Stack<Action>();
	}

	public void addAction(Action action) {
		this.actions.add(action);
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	public Stack<Action> getActions() {
		return (Stack<Action>) this.actions.clone();
	}
}