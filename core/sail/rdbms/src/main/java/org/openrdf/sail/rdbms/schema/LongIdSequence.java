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
package org.openrdf.sail.rdbms.schema;

import java.sql.SQLException;
import java.sql.Types;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.openrdf.model.Value;

/**
 * 
 * @author James Leigh
 */
public class LongIdSequence extends IdSequence {

	private long SPAN = 1152921504606846975l;

	private int SHIFT = Long.toBinaryString(SPAN).length();

	private Number[] minIds;

	private ConcurrentMap<ValueType, AtomicLong> seq = new ConcurrentHashMap<ValueType, AtomicLong>();

	public int getShift() {
		return SHIFT;
	}

	public int getJdbcIdType() {
		return Types.BIGINT;
	}

	public String getSqlType() {
		return "BIGINT";
	}

	public void init()
		throws SQLException
	{
		minIds = new Number[ValueType.values().length];
		for (int i = 0; i < minIds.length; i++) {
			minIds[i] = i * (SPAN + 1);
		}
		if (getHashTable() != null) {
			for (Number max : getHashTable().maxIds(getShift(), getMod())) {
				ValueType code = valueOf(max);
				if (max.longValue() > minId(code).longValue()) {
					if (!seq.containsKey(code)
							|| seq.get(code).longValue() < max.longValue()) {
						seq.put(code, new AtomicLong(max.longValue()));
					}
				}
			}
		}
	}

	public Number idOf(Number number) {
		return number.longValue();
	}

	public Number maxId(ValueType type) {
		return minId(type).longValue() + SPAN;
	}

	public Number minId(ValueType type) {
		return minIds[type.index()];
	}

	public Number nextId(Value value) {
		ValueType code = valueOf(value);
		if (!seq.containsKey(code)) {
			seq.putIfAbsent(code, new AtomicLong(minId(code).longValue()));
		}
		return seq.get(code).incrementAndGet();
	}

	@Override
	protected int shift(Number id) {
		return (int)(id.longValue() >>> SHIFT);
	}
}
