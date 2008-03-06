/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.tags;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.openrdf.model.Value;
import org.openrdf.queryresult.Binding;
import org.openrdf.queryresult.TupleQueryResult;
import org.openrdf.queryresult.Solution;

public class QueryResultTag extends TagSupport {

	private static final long serialVersionUID = -4566509855344444953L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
	 */
	public int doStartTag()
		throws JspException
	{
		HttpServletRequest request = (HttpServletRequest)pageContext.getRequest();
		TupleQueryResult tupleQueryResult = (TupleQueryResult)request.getAttribute("result");

		StringBuffer result = new StringBuffer();

		List<String> columns = tupleQueryResult.getBindingNames();

		int resultCount = 0;

		if (tupleQueryResult != null) {

			result.append("<table class=\"queryResult\">");
			result.append("<tr>");
			for (String columnName : columns) {
				result.append("<th>" + columnName + "</th>");
			}
			result.append("</tr>");

			for (Solution solution : tupleQueryResult) {
				resultCount++;
				result.append("<tr>");
				for (Binding binding : solution) {
					result.append("<td>");
					Value value = binding.getValue();
					if (value != null) {
						result.append(value.toString());
					}
					result.append("</td>");
				}
				result.append("</tr>");
			}
			result.append("<tr><th colspan=" + columns.size() + ">(" + resultCount + " results found)</th></tr>");
			result.append("</table>");
		}

		// write the result to the page
		JspWriter out = pageContext.getOut();
		try {
			out.write(result.toString());
		}
		catch (IOException e) {
			throw new JspException(e);
		}

		return SKIP_BODY;

	}

}
