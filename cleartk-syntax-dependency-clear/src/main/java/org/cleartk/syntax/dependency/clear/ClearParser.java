package org.cleartk.syntax.dependency.clear;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.syntax.dependency.type.DependencyNode;
import org.cleartk.token.type.Sentence;
import org.cleartk.token.type.Token;
import org.cleartk.util.UIMAUtil;
import org.uimafit.component.JCasAnnotator_ImplBase;
import org.uimafit.descriptor.ConfigurationParameter;
import org.uimafit.descriptor.TypeCapability;
import org.uimafit.factory.ConfigurationParameterFactory;
import org.uimafit.util.JCasUtil;

import clear.dep.DepNode;
import clear.dep.DepTree;
import clear.parse.AbstractDepParser;

/**
 * This class provides a UIMA wrapper for the CLEAR parser.  This parser is available here:
 * <p>
 * http://code.google.com/p/clearparser/
 * <p>
 * Please see /cleartk-syntax-dependency-clear/src/main/resources/org/cleartk/syntax/dependency/clear/README
 * for important information pertaining to the models provided for this parser.  In particular, note that 
 * the output of the CLEAR parser is different than that of the Malt parser and so these two parsers may not
 * be interchangeable (without some effort) for most use cases.
 * <p>   
 * @author Philip Ogren
 *
 */
@TypeCapability(inputs = { "org.cleartk.token.type.Token:pos",
		"org.cleartk.token.type.Token:lemma" })
public class ClearParser extends JCasAnnotator_ImplBase {

	public static final String PARAM_PARSER_MODEL_FILE_NAME = ConfigurationParameterFactory
			.createConfigurationParameterName(ClearParser.class,
					"parserModelFileName");
	@ConfigurationParameter(defaultValue = "../cleartk-syntax-dependency-clear/src/main/resources/org/cleartk/syntax/dependency/clear/conll-2009-dev-shift-pop.jar", mandatory = true, description = "This parameter provides the file name of the dependency parser model required by the factory method provided by ClearParserUtil.")
	private String parserModelFileName;

	public static final String PARAM_PARSER_ALGORITHM_NAME = ConfigurationParameterFactory
			.createConfigurationParameterName(ClearParser.class,
					"parserAlgorithmName");

	@ConfigurationParameter(defaultValue = AbstractDepParser.ALG_SHIFT_POP, mandatory = true, description = "This parameter provides the algorithm name used by the dependency parser that is required by the factory method provided by ClearParserUtil.  If in doubt, do not change from the default value.")
	private String parserAlgorithmName;

	private AbstractDepParser parser;

	@Override
	public void initialize(UimaContext context)
			throws ResourceInitializationException {
		super.initialize(context);

		try {
			parser = ClearParserUtil.createParser(parserModelFileName,
					parserAlgorithmName);
		} catch (IOException e) {
			throw new ResourceInitializationException(e);
		}
	}

	@Override
	public void process(JCas jCas) throws AnalysisEngineProcessException {
		for (Sentence sentence : JCasUtil.select(jCas, Sentence.class)) {
			List<Token> tokens = JCasUtil.selectCovered(jCas, Token.class,
					sentence);
			DepTree tree = new DepTree();

			for (int i = 0; i < tokens.size(); i++) {
				Token token = tokens.get(i);
				DepNode node = new DepNode();
				node.id = i + 1;
				node.form = token.getCoveredText();
				node.pos = token.getPos();
				node.lemma = token.getLemma();
				tree.add(node);
			}
			parser.parse(tree);
			System.out.println(tree);

			addTree(jCas, sentence, tokens, tree);
		}
	}

	private void addTree(JCas jCas, Sentence sentence, List<Token> tokens, DepTree tree) {
		Map<DependencyNode, List<DependencyNode>> nodeChildren = new HashMap<DependencyNode, List<DependencyNode>>();

		DependencyNode[] nodes = new DependencyNode[tree.size()];

		DepNode parserRootNode = tree.get(0);
		DependencyNode rootNode = new DependencyNode(jCas, sentence.getBegin(), sentence.getEnd());
		rootNode.setDependencyType(parserRootNode.deprel);
		rootNode.addToIndexes();
		nodes[0] = rootNode;

		for (int i = 0; i < tokens.size(); i++) {
			Token token = tokens.get(i);
			DepNode parserNode = tree.get(i + 1);
			DependencyNode node = new DependencyNode(jCas, token.getBegin(),
					token.getEnd());
			node.setDependencyType(parserNode.deprel);
			node.addToIndexes();
			nodes[i + 1] = node;
		}

		for (int i = 0; i < tree.size(); i++) {
			DepNode parserNode = tree.get(i);
			if (parserNode.hasHead) {
				int headIndex = parserNode.headId;

				DependencyNode node = nodes[i];
				DependencyNode headNode = nodes[headIndex];
				node.setHead(headNode);

				// collect child information
				if (!nodeChildren.containsKey(headNode)) {
					nodeChildren.put(headNode, new ArrayList<DependencyNode>());
				}
				nodeChildren.get(headNode).add(node);
			}
		}

		// add child links between node annotations
		for (DependencyNode headNode : nodeChildren.keySet()) {
			headNode.setChildren(UIMAUtil.toFSArray(jCas,
					nodeChildren.get(headNode)));
		}
		for (DependencyNode node : JCasUtil.iterate(jCas, DependencyNode.class)) {
			if (node.getChildren() == null) {
				node.setChildren(new FSArray(jCas, 0));
			}
		}

	}

}