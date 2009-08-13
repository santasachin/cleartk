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
package org.cleartk.tfidf;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.Map;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.collection.CollectionReader;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.cleartk.sentence.opennlp.OpenNLPSentenceSegmenter;
import org.cleartk.token.TokenAnnotator;
import org.cleartk.token.snowball.DefaultSnowballStemmer;
import org.cleartk.token.snowball.SnowballStemmer;
import org.cleartk.type.Sentence;
import org.cleartk.type.Token;
import org.cleartk.util.FilesCollectionReader;
import org.cleartk.util.ReflectionUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.uutuc.factory.AnalysisEngineFactory;
import org.uutuc.factory.CollectionReaderFactory;
import org.uutuc.factory.TypeSystemDescriptionFactory;
import org.uutuc.util.JCasIterable;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

 *
 * @author Steven Bethard
 *
 */
public class InverseDocumentFrequencyWriterTest {
	
	protected final String inputDir = "test/data/text";
	protected final String mapFileName = "test/data/docfreq.obj";
	protected Map<String, Integer> frequencies;

	@After
	public void tearDown() throws Exception {
		File mapFile = new File(this.mapFileName);
		if (mapFile.exists()) {
			mapFile.delete();
		}
	}

	@Test
	public void test() throws Exception {
		TypeSystemDescription typeSystem = TypeSystemDescriptionFactory.createTypeSystemDescription(
				Sentence.class, Token.class);
		CollectionReader reader = CollectionReaderFactory.createCollectionReader(
				FilesCollectionReader.class, typeSystem,
				FilesCollectionReader.PARAM_FILE_OR_DIRECTORY, this.inputDir);
		AnalysisEngine[] engines = new AnalysisEngine[] {
				AnalysisEngineFactory.createPrimitive(OpenNLPSentenceSegmenter.class, typeSystem,
						OpenNLPSentenceSegmenter.PARAM_SENTENCE_MODEL_FILE,
						"./resources/models/OpenNLP.Sentence.English.bin.gz"),
				AnalysisEngineFactory.createPrimitive(TokenAnnotator.class, typeSystem),
				AnalysisEngineFactory.createPrimitive(DefaultSnowballStemmer.class, typeSystem,
						SnowballStemmer.PARAM_STEMMER_NAME, "English"),
				AnalysisEngineFactory.createPrimitive(InverseDocumentFrequencyWriter.class, typeSystem,
						InverseDocumentFrequencyWriter.PARAM_OUTPUT_FILE, this.mapFileName),
		};
		
		for (JCas jCas: new JCasIterable(reader, engines)) {
			Assert.assertFalse(jCas == null);
		}
		reader.close();
		for (AnalysisEngine engine: engines) {
			engine.collectionProcessComplete();
		}
		
		// read the frequencies from the file
		ObjectInput input = new ObjectInputStream(new BufferedInputStream(
				new FileInputStream(this.mapFileName)));
		this.frequencies = ReflectionUtil.uncheckedCast(input.readObject());
		input.close();
		
		// check log(documentCount)
		this.checkCount(Math.log(3.0), null);
		
		// check stems
		this.checkCount(Math.log(3.0 / 2), ".");
		this.checkCount(Math.log(3.0 / 1), "and");
		this.checkCount(Math.log(3.0 / 2), "bear");
		this.checkCount(Math.log(3.0 / 2), "big");
		this.checkCount(Math.log(3.0 / 1), "bit");
		this.checkCount(Math.log(3.0 / 1), "black");
		this.checkCount(Math.log(3.0 / 1), "bled");
		this.checkCount(Math.log(3.0 / 1), "blood");
		this.checkCount(Math.log(3.0 / 2), "brown");
		this.checkCount(Math.log(3.0 / 1), "bug");
		this.checkCount(Math.log(3.0 / 1), "dog");
		this.checkCount(Math.log(3.0 / 2), "fox");
		this.checkCount(Math.log(3.0 / 2), "jump");
		this.checkCount(Math.log(3.0 / 2), "lazi");
		this.checkCount(Math.log(3.0 / 2), "over");
		this.checkCount(Math.log(3.0 / 1), "quick");
		this.checkCount(Math.log(3.0 / 3), "the");

		// check original, non-stems
		this.checkCount(null, "jumped");
		this.checkCount(null, "lazy");
	}
	
	@Test
	public void testDescriptor() throws Exception {
		String descPath = "org.cleartk.tfidf.InverseDocumentFrequencyWriter";
		try {
			AnalysisEngineFactory.createAnalysisEngine(descPath);
			Assert.fail("Expected exception with no OutputFile");
		} catch (ResourceInitializationException e) {}
		
		AnalysisEngine engine = AnalysisEngineFactory.createAnalysisEngine(descPath,
				InverseDocumentFrequencyWriter.PARAM_OUTPUT_FILE, this.mapFileName);
		String fileName = (String)engine.getConfigParameterValue(
				InverseDocumentFrequencyWriter.PARAM_OUTPUT_FILE);
		Assert.assertEquals(this.mapFileName, fileName);
	}
	
	private void checkCount(Double expected, String word) {
		Assert.assertEquals(expected, this.frequencies.get(word));
	}
}