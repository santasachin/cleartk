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
package org.cleartk.token.chunk;

import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;
import org.cleartk.chunk.ChunkerFeatureExtractor;
import org.cleartk.classifier.Instance;
import org.cleartk.classifier.feature.WindowFeature;
import org.cleartk.classifier.feature.extractor.SimpleFeatureExtractor;
import org.cleartk.classifier.feature.extractor.SpannedTextExtractor;
import org.cleartk.classifier.feature.extractor.WindowExtractor;
import org.cleartk.classifier.feature.proliferate.CharacterNGramProliferator;
import org.cleartk.classifier.feature.proliferate.LowerCaseProliferator;
import org.cleartk.classifier.feature.proliferate.ProliferatingExtractor;
import org.cleartk.type.Token;


/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

*/

public class TestChunkFeatureExtractor implements ChunkerFeatureExtractor {

	private List<SimpleFeatureExtractor> simpleFeatureExtractors;
	private List<WindowExtractor> windowExtractors;

	public Instance<String> extractFeatures(JCas jCas, Annotation labeledAnnotation, Annotation sequence) {
		Instance<String> instance = new Instance<String>();
		
		// extract all features that require only the token annotation
		for (SimpleFeatureExtractor extractor: this.simpleFeatureExtractors) {
			instance.addAll(extractor.extract(jCas, labeledAnnotation));
		}
		
		// extract all features that require the token and sentence annotations
		for (WindowExtractor extractor: this.windowExtractors) {
			instance.addAll(extractor.extract(jCas, labeledAnnotation, sequence));
		}

		return instance;
	}

	public void initialize(UimaContext context) throws ResourceInitializationException {
				this.simpleFeatureExtractors = new ArrayList<SimpleFeatureExtractor>();
		this.windowExtractors = new ArrayList<WindowExtractor>();
		
		SimpleFeatureExtractor wordExtractor = new SpannedTextExtractor();
		
		int fromLeft = CharacterNGramProliferator.LEFT_TO_RIGHT;
		this.simpleFeatureExtractors.add(new ProliferatingExtractor(
				wordExtractor,
				new LowerCaseProliferator(),
				new CharacterNGramProliferator(fromLeft, 0, 3, 3, true)));
		
		this.windowExtractors.add(new WindowExtractor(
				Token.class, wordExtractor, WindowFeature.ORIENTATION_LEFT, 0, 2));
		
	}

}