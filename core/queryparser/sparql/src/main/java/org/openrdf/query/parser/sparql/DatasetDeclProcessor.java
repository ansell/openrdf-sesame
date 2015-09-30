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
package org.openrdf.query.parser.sparql;

import java.util.List;

import org.openrdf.model.IRI;
import org.openrdf.model.impl.SimpleIRI;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.SESAME;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.impl.SimpleDataset;
import org.openrdf.query.parser.sparql.ast.ASTDatasetClause;
import org.openrdf.query.parser.sparql.ast.ASTIRI;
import org.openrdf.query.parser.sparql.ast.ASTOperation;
import org.openrdf.query.parser.sparql.ast.ASTOperationContainer;

/**
 * Extracts a SPARQL {@link Dataset} from an ASTQueryContainer, if one is
 * contained.
 * 
 * @author Simon Schenk
 * @author Arjohn Kampman
 */
public class DatasetDeclProcessor {

	/**
	 * Extracts a SPARQL {@link Dataset} from an ASTQueryContainer, if one is
	 * contained. Returns null otherwise.
	 * 
	 * @param qc
	 *        The query model to resolve relative URIs in.
	 * @throws MalformedQueryException
	 *         If DatasetClause does not contain a valid URI.
	 */
	public static Dataset process(ASTOperationContainer qc)
		throws MalformedQueryException
	{
		SimpleDataset dataset = null;

		ASTOperation op = qc.getOperation();
		if (op != null) {

			List<ASTDatasetClause> datasetClauses = op.getDatasetClauseList();

			if (!datasetClauses.isEmpty()) {
				dataset = new SimpleDataset();

				for (ASTDatasetClause dc : datasetClauses) {

					ASTIRI astIri = dc.jjtGetChild(ASTIRI.class);

					try {
						IRI uri = SESAME.NIL;
						
						if (astIri != null) {
							uri = SimpleValueFactory.getInstance().createIRI(astIri.getValue());
						}
						
						if (dc.isNamed()) {
							dataset.addNamedGraph(uri);
						}
						else {
							dataset.addDefaultGraph(uri);
						}
					}
					catch (IllegalArgumentException e) {
						throw new MalformedQueryException(e.getMessage(), e);
					}
				}
			}
		}

		return dataset;
	}
}
