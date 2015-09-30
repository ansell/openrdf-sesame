/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra.evaluation.function.string;

import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.FN;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.algebra.evaluation.ValueExprEvaluationException;
import org.eclipse.rdf4j.query.algebra.evaluation.function.Function;
import org.eclipse.rdf4j.query.algebra.evaluation.util.QueryEvaluationUtil;

/**
 * The SPARQL built-in {@link Function} REPLACE, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class Replace implements Function {

	public String getURI() {
		return FN.REPLACE.toString();
	}

	public Literal evaluate(ValueFactory valueFactory, Value... args)
		throws ValueExprEvaluationException
	{
		if (args.length < 3 || args.length > 4) {
			throw new ValueExprEvaluationException("Incorrect number of arguments for REPLACE: " + args.length);
		}

		try {
			Literal arg = (Literal)args[0];
			Literal pattern = (Literal)args[1];
			Literal replacement = (Literal)args[2];
			Literal flags = null;
			if (args.length == 4) {
				flags = (Literal)args[3];
			}

			if (! QueryEvaluationUtil.isStringLiteral(arg)) {
				throw new ValueExprEvaluationException("incompatible operand for REPLACE: " + arg);
			}

			if (! QueryEvaluationUtil.isSimpleLiteral(pattern)) {
				throw new ValueExprEvaluationException("incompatible operand for REPLACE: " + pattern);
			}

			if (! QueryEvaluationUtil.isSimpleLiteral(replacement)) {
				throw new ValueExprEvaluationException("incompatible operand for REPLACE: " + replacement);
			}

			String flagString = null;
			if (flags != null) {
				if (!QueryEvaluationUtil.isSimpleLiteral(flags)) {
					throw new ValueExprEvaluationException("incompatible operand for REPLACE: " + flags);
				}
				flagString = flags.getLabel();
			}

			String argString = arg.getLabel();
			String patternString = pattern.getLabel();
			String replacementString = replacement.getLabel();

			int f = 0;
			if (flagString != null) {
				for (char c : flagString.toCharArray()) {
					switch (c) {
						case 's':
							f |= Pattern.DOTALL;
							break;
						case 'm':
							f |= Pattern.MULTILINE;
							break;
						case 'i':
							f |= Pattern.CASE_INSENSITIVE;
							break;
						case 'x':
							f |= Pattern.COMMENTS;
							break;
						case 'd':
							f |= Pattern.UNIX_LINES;
							break;
						case 'u':
							f |= Pattern.UNICODE_CASE;
							break;
						default:
							throw new ValueExprEvaluationException(flagString);
					}
				}
			}

			Pattern p = Pattern.compile(patternString, f);
			String result = p.matcher(argString).replaceAll(replacementString);

			Optional<String> lang = arg.getLanguage();
			IRI dt = arg.getDatatype();

			if (lang.isPresent()) {
				return valueFactory.createLiteral(result, lang.get());
			}
			else if (dt != null) {
				return valueFactory.createLiteral(result, dt);
			}
			else {
				return valueFactory.createLiteral(result);
			}
		}
		catch (ClassCastException e) {
			throw new ValueExprEvaluationException("literal operands expected", e);
		}

	}
}
