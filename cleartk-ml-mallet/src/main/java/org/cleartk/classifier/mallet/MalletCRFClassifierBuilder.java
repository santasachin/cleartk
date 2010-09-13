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
package org.cleartk.classifier.mallet;

import java.io.File;

import org.cleartk.classifier.jar.BuildJar;
import org.cleartk.classifier.jar.ClassifierBuilder;

import cc.mallet.fst.SimpleTagger;

/**
 * <br>Copyright (c) 2007-2008, Regents of the University of Colorado 
 * <br>All rights reserved.

* 
* @author Philip Ogren
* 
*/

public class MalletCRFClassifierBuilder implements ClassifierBuilder<String> {

	public void train(File dir, String[] args) throws Exception {
		String[] malletArgs = new String[args.length + 7];
		System.arraycopy(args, 0, malletArgs, 0, args.length);
		malletArgs[malletArgs.length - 7] = "--train";
		malletArgs[malletArgs.length - 6] = "true";
		malletArgs[malletArgs.length - 5] = "--test";
		malletArgs[malletArgs.length - 4] = "lab";
		malletArgs[malletArgs.length - 3] = "--model-file";
		malletArgs[malletArgs.length - 2] = new File(dir, "model.malletcrf").getPath();
		malletArgs[malletArgs.length - 1] = new File(dir, "training-data.malletcrf").getPath();
		SimpleTagger.main(malletArgs);
	}

	public void buildJar(File dir, String[] args) throws Exception {
		BuildJar.OutputStream stream = new BuildJar.OutputStream(dir);
		stream.write("model.malletcrf", new File(dir, "model.malletcrf"));
		stream.close();
	}

	public Class<?> getClassifierClass() {
		return MalletCRFClassifier.class;
	}

}