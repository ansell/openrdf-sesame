package org.openrdf.model;

import junit.framework.Test;

import org.openrdf.model.impl.LinkedHashModel;

/**
 * ValueFactory is not serializable. This test ensures that the
 * LinkedHashModel's getValueFactory() does not try to serialize a ValueFactory
 * with it.
 * 
 * @author James Leigh
 */
public class LinkedHashModelWithValueFactoryTest extends ModelTest {

	public static Test suite() throws Exception {
		return ModelTest.suite(LinkedHashModelWithValueFactoryTest.class);
	}

	public LinkedHashModelWithValueFactoryTest(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		LinkedHashModel model = new LinkedHashModel();
		model.getValueFactory();
		return model;
	}
}
