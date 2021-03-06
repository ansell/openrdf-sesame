/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.memory.model;

import org.openrdf.model.impl.SimpleBNode;

/**
 * A MemoryStore-specific extension of BNodeImpl giving it node properties.
 */
public class MemBNode extends SimpleBNode implements MemResource {

	private static final long serialVersionUID = -887382892580321647L;

	/*------------*
	 * Attributes *
	 *------------*/

	/**
	 * The object that created this MemBNode.
	 */
	transient final private Object creator;

	/**
	 * The list of statements for which this MemBNode is the subject.
	 */
	transient private volatile MemStatementList subjectStatements;

	/**
	 * The list of statements for which this MemBNode is the object.
	 */
	transient private volatile MemStatementList objectStatements;

	/**
	 * The list of statements for which this MemBNode represents the context.
	 */
	transient private volatile MemStatementList contextStatements;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new MemBNode for a bnode ID.
	 * 
	 * @param creator
	 *        The object that is creating this MemBNode.
	 * @param id
	 *        bnode ID.
	 */
	public MemBNode(Object creator, String id) {
		super(id);
		this.creator = creator;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Object getCreator() {
		return creator;
	}

	public boolean hasStatements() {
		return subjectStatements != null || objectStatements != null || contextStatements != null;
	}

	public MemStatementList getSubjectStatementList() {
		if (subjectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return subjectStatements;
		}
	}

	public int getSubjectStatementCount() {
		if (subjectStatements == null) {
			return 0;
		}
		else {
			return subjectStatements.size();
		}
	}

	public void addSubjectStatement(MemStatement st) {
		if (subjectStatements == null) {
			subjectStatements = new MemStatementList(4);
		}

		subjectStatements.add(st);
	}

	public void removeSubjectStatement(MemStatement st) {
		subjectStatements.remove(st);

		if (subjectStatements.isEmpty()) {
			subjectStatements = null;
		}
	}

	public void cleanSnapshotsFromSubjectStatements(int currentSnapshot) {
		if (subjectStatements != null) {
			subjectStatements.cleanSnapshots(currentSnapshot);

			if (subjectStatements.isEmpty()) {
				subjectStatements = null;
			}
		}
	}

	public MemStatementList getObjectStatementList() {
		if (objectStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return objectStatements;
		}
	}

	public int getObjectStatementCount() {
		if (objectStatements == null) {
			return 0;
		}
		else {
			return objectStatements.size();
		}
	}

	public void addObjectStatement(MemStatement st) {
		if (objectStatements == null) {
			objectStatements = new MemStatementList(4);
		}

		objectStatements.add(st);
	}

	public void removeObjectStatement(MemStatement st) {
		objectStatements.remove(st);

		if (objectStatements.isEmpty()) {
			objectStatements = null;
		}
	}

	public void cleanSnapshotsFromObjectStatements(int currentSnapshot) {
		if (objectStatements != null) {
			objectStatements.cleanSnapshots(currentSnapshot);

			if (objectStatements.isEmpty()) {
				objectStatements = null;
			}
		}
	}

	public MemStatementList getContextStatementList() {
		if (contextStatements == null) {
			return EMPTY_LIST;
		}
		else {
			return contextStatements;
		}
	}

	public int getContextStatementCount() {
		if (contextStatements == null) {
			return 0;
		}
		else {
			return contextStatements.size();
		}
	}

	public void addContextStatement(MemStatement st) {
		if (contextStatements == null) {
			contextStatements = new MemStatementList(4);
		}

		contextStatements.add(st);
	}

	public void removeContextStatement(MemStatement st) {
		contextStatements.remove(st);

		if (contextStatements.isEmpty()) {
			contextStatements = null;
		}
	}

	public void cleanSnapshotsFromContextStatements(int currentSnapshot) {
		if (contextStatements != null) {
			contextStatements.cleanSnapshots(currentSnapshot);

			if (contextStatements.isEmpty()) {
				contextStatements = null;
			}
		}
	}
}
