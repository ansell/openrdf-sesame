package org.openrdf.model;

import junit.framework.Test;

import org.openrdf.model.impl.TreeModel;

public class TreeModelTest extends ModelTest {

	public static Test suite() throws Exception {
		return ModelTest.suite(TreeModelTest.class);
	}

	public TreeModelTest(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		return new TreeModel();
	}
}
