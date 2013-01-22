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
public class TestLinkedHashModelWithValueFactory extends TestModel {

	public static Test suite() throws Exception {
		return TestModel.suite(TestLinkedHashModelWithValueFactory.class);
	}

	public TestLinkedHashModelWithValueFactory(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		LinkedHashModel model = new LinkedHashModel();
		model.getValueFactory();
		return model;
	}
}
