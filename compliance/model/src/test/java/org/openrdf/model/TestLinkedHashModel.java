package org.openrdf.model;

import junit.framework.Test;

import org.openrdf.model.impl.LinkedHashModel;

public class TestLinkedHashModel extends TestModel {

	public static Test suite() throws Exception {
		return TestModel.suite(TestLinkedHashModel.class);
	}

	public TestLinkedHashModel(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		return new LinkedHashModel();
	}
}
