/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.memory;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.evaluation.impl.CostComparator;
import org.openrdf.sail.memory.model.MemResource;
import org.openrdf.sail.memory.model.MemURI;
import org.openrdf.sail.memory.model.MemValue;
import org.openrdf.sail.memory.model.MemValueFactory;

/**
 * Uses the MemoryStore's statement sizes to give cost estimates based on the
 * size of the expected results. This process could be improved with repository
 * statistics about size and distribution of statements.
 * 
 * @author James Leigh <james@leighnet.ca>
 * 
 */
class MemoryStoreCostComparator extends CostComparator {

	private MemoryStore _store;

	public MemoryStoreCostComparator(MemoryStore store) {
		_store = store;
	}

	@Override
	public void meet(StatementPattern sp) {
		Resource subj = (Resource) getConstantValue(sp.getSubjectVar());
		URI pred = (URI) getConstantValue(sp.getPredicateVar());
		Value obj = getConstantValue(sp.getObjectVar());
		Resource context = (Resource) getConstantValue(sp.getContextVar());

		int unknown = 0;
		if (subj == null)
			unknown++;
		if (pred == null)
			unknown++;
		if (obj == null)
			unknown++;
		if (context == null)
			unknown++;

		if (unknown == 0) {
			// exact match
			_cost = 1;
			return;
		} else if (unknown == 4) {
			// wild card
			_cost = _store.size();
			return;
		}

		MemValueFactory valueFactory = _store.getValueFactory();

		MemResource memSubj = null;
		MemURI memPred = null;
		MemValue memObj = null;
		MemResource memContext = null;

		// Perform look-ups for value-equivalents of the specified values
		if (subj != null) {
			memSubj = valueFactory.getMemResource(subj);
			if (memSubj == null) {
				// non-existent subject
				_cost = 0;
				return;
			}
		}
		if (pred != null) {
			memPred = valueFactory.getMemURI(pred);
			if (memPred == null) {
				// non-existent predicate
				_cost = 0;
				return;
			}
		}
		if (obj != null) {
			memObj = valueFactory.getMemValue(obj);
			if (memObj == null) {
				// non-existent object
				_cost = 0;
				return;
			}
		}
		if (context != null) {
			memContext = valueFactory.getMemResource(context);
			if (memContext == null) {
				// non-existent context
				_cost = 0;
				return;
			}
		}

		// Search for the smallest list that can be used by the iterator
		int smallestList = _store.size();

		if (memSubj != null) {
			int l = memSubj.getSubjectStatementCount();
			if (l < smallestList) {
				smallestList = l;
			}
		}
		if (memPred != null) {
			int l = memPred.getPredicateStatementCount();
			if (l < smallestList) {
				smallestList = l;
			}
		}
		if (memObj != null) {
			int l = memObj.getObjectStatementCount();
			if (l < smallestList) {
				smallestList = l;
			}
		}
		if (memContext != null) {
			int l = memContext.getContextStatementCount();
			if (l < smallestList) {
				smallestList = l;
			}
		}

		switch (unknown) {
		case 1:
			// statistics would be nice here
			_cost = (smallestList + 3) / 4;
			break;
		case 2:
			// statistics would be nice here
			_cost = (smallestList + 1) / 2;
			break;
		default:
			assert unknown == 3;
			_cost = smallestList;
			break;
		}
	}

}
