package org.openrdf.model;

import junit.framework.Test;

import org.openrdf.model.impl.TreeModel;

public class TestTreeModel extends TestModel {

	public static Test suite() throws Exception {
		return TestModel.suite(TestTreeModel.class);
	}

	public TestTreeModel(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		return new TreeModel();
	}
}
