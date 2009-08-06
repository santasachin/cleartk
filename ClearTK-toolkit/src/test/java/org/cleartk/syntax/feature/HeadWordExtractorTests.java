 /** 
 * Copyright (c) 2007-2008, Regents of the University of Colorado 
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution. 
 * Neither the name of the University of Colorado at Boulder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE. 
*/
package org.cleartk.syntax.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.uima.UIMAException;
import org.apache.uima.jcas.JCas;
import org.cleartk.classifier.Feature;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.TypePathExtractor;
import org.cleartk.syntax.TreebankTestsUtil;
import org.cleartk.syntax.feature.HeadWordExtractor;
import org.cleartk.syntax.treebank.type.TreebankNode;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.JCasFactory;
import org.uutuc.factory.TokenFactory;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 * 
 * @author Steven Bethard
 */

public class HeadWordExtractorTests {

	@Test
	public void testNoTreebankNode() throws UIMAException {
		HeadWordExtractor extractor = new HeadWordExtractor(null);
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText("foo");
		Token token = new Token(jCas, 0, 3);
		token.addToIndexes();

		this.checkFeatures(extractor.extract(jCas, token), null);
	}
	
	@Test
	public void testNoTokens() throws UIMAException {
		HeadWordExtractor extractor = new HeadWordExtractor(new SpannedTextExtractor(), true);
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText("foo");
		TreebankNode node = TreebankTestsUtil.newNode(jCas, 0, 3, "NN");

		this.checkFeatures(extractor.extract(jCas, node), null);
	}
	
	@Test
	public void testNoNodeTypes() throws UIMAException {
		HeadWordExtractor extractor = new HeadWordExtractor(null);
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		jCas.setDocumentText("foo");
		TreebankNode parent = TreebankTestsUtil.newNode(jCas, null, TreebankTestsUtil.newNode(jCas, 0, 3, null));

		this.checkFeatures(extractor.extract(jCas, parent), null);
	}
	
	@Test
	public void testSimpleSentence() throws UIMAException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		TokenFactory.createTokens(jCas, "I ran home", Token.class, Sentence.class, null, "PRP VBD NN", null, "org.cleartk.type.Token:pos", null);
		TreebankNode iNode = TreebankTestsUtil.newNode(jCas, 0, 1, "PRP");
		TreebankNode ranNode = TreebankTestsUtil.newNode(jCas, 2, 5, "VBD");
		TreebankNode homeNode = TreebankTestsUtil.newNode(jCas, 6, 10, "NN");
		TreebankNode vpNode = TreebankTestsUtil.newNode(jCas, "VP", ranNode, homeNode);
		
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor posExtractor = new TypePathExtractor(Token.class, "pos");
		HeadWordExtractor extractor = new HeadWordExtractor("Foo", textExtractor);
		
		this.checkFeatures(
				extractor.extract(jCas, iNode),
				"Foo_HeadWord:Token_SpannedText", "I");
		
		this.checkFeatures(
				extractor.extract(jCas, vpNode),
				"Foo_HeadWord:Token_SpannedText", "ran");
		
		this.checkFeatures(
				new HeadWordExtractor(textExtractor, false).extract(jCas, vpNode),
				"HeadWord:Node_SpannedText", "ran");

		this.checkFeatures(
				new HeadWordExtractor(posExtractor).extract(jCas, vpNode),
				"HeadWord:Token_TypePath_Pos", "VBD");
}
	
	@Test
	public void testNPandPP() throws UIMAException {
		JCas jCas = JCasFactory.createJCas("org.cleartk.TypeSystem");
		TokenFactory.createTokens(jCas,
				"cat's toy under the box", Token.class, Sentence.class, 
				"cat 's toy under the box",
				"NN POS NN IN DT NN",
				null, "org.cleartk.type.Token:pos", null);
		TreebankNode catNode = TreebankTestsUtil.newNode(jCas, 0, 3, "NN");
		TreebankNode sNode = TreebankTestsUtil.newNode(jCas, 3, 5, "POS");
		TreebankNode catsNode = TreebankTestsUtil.newNode(jCas, "NP", catNode, sNode);
		TreebankNode toyNode = TreebankTestsUtil.newNode(jCas, 6, 9, "NN");
		TreebankNode catstoyNode = TreebankTestsUtil.newNode(jCas, "NP", catsNode, toyNode);
		TreebankNode underNode = TreebankTestsUtil.newNode(jCas, 10, 15, "IN");
		TreebankNode theNode = TreebankTestsUtil.newNode(jCas, 16, 19, "DT");
		TreebankNode boxNode = TreebankTestsUtil.newNode(jCas, 20, 23, "NN");
		TreebankNode theboxNode = TreebankTestsUtil.newNode(jCas, "NP", theNode, boxNode);
		TreebankNode undertheboxNode = TreebankTestsUtil.newNode(jCas, "PP", underNode, theboxNode);
		TreebankNode tree = TreebankTestsUtil.newNode(jCas, "NP", catstoyNode, undertheboxNode);
		
		SpannedTextExtractor textExtractor = new SpannedTextExtractor();
		TypePathExtractor posExtractor = new TypePathExtractor(Token.class, "pos");
		HeadWordExtractor extractor;
		
		extractor = new HeadWordExtractor(posExtractor, true, true);
		this.checkFeatures(
				extractor.extract(jCas, tree),
				"HeadWord:Token_TypePath_Pos", "NN");
		this.checkFeatures(
				extractor.extract(jCas, undertheboxNode),
				"HeadWord:Token_TypePath_Pos", "IN");
		
		extractor = new HeadWordExtractor(textExtractor, false, true);
		this.checkFeatures(
				extractor.extract(jCas, tree),
				"HeadWord:Node_SpannedText", "toy");
		
		List<Feature> features = extractor.extract(jCas, undertheboxNode);
		Assert.assertEquals(2, features.size());
		Assert.assertEquals("HeadWord:Node_SpannedText", features.get(0).getName());
		Assert.assertEquals("under", features.get(0).getValue());
		Assert.assertEquals("PPHeadWord:Node_SpannedText", features.get(1).getName());
		Assert.assertEquals("the box", features.get(1).getValue());

	}
	
	private void checkFeatures(List<Feature> features, String expectedName, Object ... expectedValues) {
		List<Object> actualValues = new ArrayList<Object>();
		for (Feature feature: features) {
			Assert.assertEquals(expectedName, feature.getName());
			actualValues.add(feature.getValue());
		}
		Assert.assertEquals(Arrays.asList(expectedValues), actualValues);
		
	}
	
}