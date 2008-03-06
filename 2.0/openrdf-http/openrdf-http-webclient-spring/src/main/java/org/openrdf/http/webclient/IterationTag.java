/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iteration;

/**
 * 
 * @author Herko ter Horst
 */
public class IterationTag extends LoopTagSupport {

	final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private Iteration<?, ?> items;

	private static final long serialVersionUID = -3584116873035047009L;

	@Override
	protected boolean hasNext()
		throws JspTagException
	{
		boolean result = false;
		try {
			logger.debug("iteration hasNext()?");
			result = items.hasNext();
		}
		catch (Exception e) {
			logger.debug("iteration.hasNext() threw Exception");
			throw new JspTagException(e);
		}
		return result;
	}

	@Override
	protected Object next()
		throws JspTagException
	{
		Object result = null;
		try {
			logger.debug("iteration next()...");
			result = items.next();
			if(result == null) {
				logger.debug("iteration.next() returned null");
			}
		}
		catch (Exception e) {
			logger.debug("iteration.next() threw Exception");
			throw new JspTagException(e);
		}
		return result;
	}

	@Override
	protected void prepare()
		throws JspTagException
	{
		// do nothing
	}

	/**
	 * @param items
	 *        The items to set.
	 */
	public void setItems(Iteration<?, ?> items) {
		this.items = items;
	}

}
