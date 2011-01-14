 /** 
 * Copyright (c) 2010, Regents of the University of Colorado 
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
package org.cleartk.syntax.opennlp.parser;

import java.util.List;

import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.cleartk.syntax.opennlp.ParserAnnotator;

/**
 * <br>
 * Copyright (c) 2010, Regents of the University of Colorado <br>
 * All rights reserved.
 * <p>
 * 
 * InputTypesHelper allows the {@link ParserAnnotator} to abstract away the input token and sentence types that it expects.  The default
 * implementation uses the ClearTK token and sentence types, but by extending this class you could specify your own input types from your 
 * type system.  
 * 
 * @author Philip Ogren
 */

public abstract class InputTypesHelper<TOKEN_TYPE extends Annotation, SENTENCE_TYPE extends Annotation> {
	public abstract List<TOKEN_TYPE> getTokens(JCas jCas, SENTENCE_TYPE sentence);

	public abstract List<SENTENCE_TYPE> getSentences(JCas jCas);
	
	public abstract String getPosTag(TOKEN_TYPE token);

	public abstract void setPosTag(TOKEN_TYPE token, String tag);

	/**
	 * There must be a better way to get around the runtime type erasure than to 
	 * introduce an additional method like this.  If you know of something more elegant
	 * than this, then please let us know! 
	 * @param token
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String getTag(Annotation token) {
		return getPosTag((TOKEN_TYPE) token);
	}

	@SuppressWarnings("unchecked")
	public void setTag(Annotation token, String tag) {
		setPosTag((TOKEN_TYPE) token, tag);
	}

}