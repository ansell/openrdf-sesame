/*
 * Licensed to Aduna under one or more contributor license agreements. See the NOTICE.txt file
 * distributed with this work for additional information regarding copyright ownership.
 * 
 * Aduna licenses this file to you under the terms of the Aduna BSD License (the "License"); you may
 * not use this file except in compliance with the License. See the LICENSE.txt file distributed
 * with this work for the full License.
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.openrdf.query.resultio;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.query.resultio.TupleQueryResultFormat;

/**
 * Abstract test for QueryResultIO.
 * 
 * @author jeen
 * @author Peter Ansell
 */
public abstract class AbstractQueryResultIOTupleTest
{
    
    /**
     * @return An example filename that will match the {@link TupleQueryResultFormat} returned by
     *         {@link #getTupleFormat()}.
     */
    protected abstract String getFileName();
    
    /**
     * @return The {@link TupleQueryResultFormat} that this test is running against.
     */
    protected abstract TupleQueryResultFormat getTupleFormat();
    
    /**
     * Test method for
     * {@link org.openrdf.query.resultio.QueryResultIO#getParserFormatForFileName(java.lang.String)}
     * .
     */
    @Test
    public final void testGetParserFormatForFileNameString()
    {
        String fileName = getFileName();
        TupleQueryResultFormat format = QueryResultIO.getParserFormatForFileName(fileName);
        
        assertNotNull("Could not find parser for this format.", format);
        assertEquals(getTupleFormat(), format);
    }
    
    @Test
    public final void testSPARQLResultFormat() throws IOException, QueryResultParseException,
        TupleQueryResultHandlerException, UnsupportedQueryResultFormatException, QueryEvaluationException
    {
        testQueryResultFormat(getTupleFormat(), createQueryResult(), createQueryResult());
    }
    
    @Test
    public final void testSPARQLResultFormatNoResults() throws IOException, QueryResultParseException,
        TupleQueryResultHandlerException, UnsupportedQueryResultFormatException, QueryEvaluationException
    {
        testQueryResultFormat(getTupleFormat(), createNoResultsQueryResult(), createNoResultsQueryResult());
    }
    
    private void testQueryResultFormat(TupleQueryResultFormat format, TupleQueryResult input, TupleQueryResult expected)
        throws IOException, QueryResultParseException, TupleQueryResultHandlerException,
        UnsupportedQueryResultFormatException, QueryEvaluationException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        QueryResultIO.write(input, format, out);
        input.close();
        
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        TupleQueryResult output = QueryResultIO.parse(in, format);
        
        assertTrue(QueryResults.equals(expected, output));
    }
    
    private TupleQueryResult createQueryResult()
    {
        List<String> bindingNames = Arrays.asList("a", "b", "c");
        
        MapBindingSet solution1 = new MapBindingSet(bindingNames.size());
        solution1.addBinding("a", new URIImpl("foo:bar"));
        solution1.addBinding("b", new BNodeImpl("bnode"));
        solution1.addBinding("c", new LiteralImpl("baz"));
        
        MapBindingSet solution2 = new MapBindingSet(bindingNames.size());
        solution2.addBinding("a", new LiteralImpl("1", XMLSchema.INTEGER));
        solution2.addBinding("c", new LiteralImpl("Hello World!", "en"));
        
        List<? extends BindingSet> bindingSetList = Arrays.asList(solution1, solution2);
        
        TupleQueryResultImpl result = new TupleQueryResultImpl(bindingNames, bindingSetList);
        
        return result;
    }
    
    private TupleQueryResult createNoResultsQueryResult()
    {
        List<String> bindingNames = Arrays.asList("a", "b", "c");
        
        List<? extends BindingSet> bindingSetList = Collections.emptyList();
        
        TupleQueryResultImpl result = new TupleQueryResultImpl(bindingNames, bindingSetList);
        
        return result;
    }
}
