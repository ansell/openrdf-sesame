package org.openrdf.model.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

public abstract class ApacheSetTestCase extends AbstractTestSet {
	private ValueFactory vf = ValueFactoryImpl.getInstance();

	public ApacheSetTestCase(String name) {
		super(name);
	}

    public void testSetEquals() {
        resetEmpty();
        assertEquals("Empty sets should be equal", 
                     getSet(), getConfirmedSet());
        verify();

        Collection set2 = makeConfirmedCollection();
        set2.add(getOneElement());
        assertTrue("Empty set shouldn't equal nonempty set", 
                   !getSet().equals(set2));

        resetFull();
        assertEquals("Full sets should be equal", getSet(), getConfirmedSet());
        verify();

        set2.clear();
        set2.addAll(Arrays.asList(getOtherElements()));
        assertTrue("Sets with different contents shouldn't be equal", 
                   !getSet().equals(set2));
    }

	public abstract Object getOneElement();

	@Override
	public boolean isNullSupported() {
		return false;
	}

	@Override
	public boolean isTestSerialization() {
		return false;
	}

	@Override
	public abstract Set makeEmptySet();

	@Override
	public abstract Collection makeConfirmedCollection();

	public Object[] getFullNonNullElements() {
		return convert(super.getFullNonNullElements());
	}

	public Object[] getOtherNonNullElements() {
		return convert(super.getOtherNonNullElements());
	}

	public abstract Object[] convert(Object[] seeds);

	public URI createURI(Object seed) {
		String prefix = "urn:test:" + seed.getClass().getSimpleName() + ":";
		if (seed instanceof Number)
			return vf.createURI(prefix + ((Number) seed).intValue());
		if (seed instanceof Character)
			return vf.createURI(prefix + ((Character) seed).hashCode());
		return vf.createURI(prefix + seed.toString());
	}

	public Value createTerm(Object seed) {
		if (seed instanceof Integer)
			return vf.createLiteral((Integer) seed);
		if (seed instanceof Double)
			return vf.createLiteral((Double) seed);
		if (seed instanceof Long)
			return vf.createLiteral((Long) seed);
		if (seed instanceof Short)
			return vf.createLiteral((Short) seed);
		if (seed instanceof Byte)
			return vf.createLiteral((Byte) seed);
		if (seed instanceof Float)
			return vf.createLiteral((Float) seed);
		if (seed instanceof Number)
			return vf.createLiteral(((Number) seed).intValue());
		if (seed instanceof Character)
			return vf.createLiteral(true);
		return vf.createLiteral(seed.toString());
	}

}
