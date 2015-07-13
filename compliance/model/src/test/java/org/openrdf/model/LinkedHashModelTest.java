package org.openrdf.model;

import junit.framework.Test;

import org.openrdf.model.impl.LinkedHashModel;

public class LinkedHashModelTest extends ModelTest {

	public static Test suite() throws Exception {
		return ModelTest.suite(LinkedHashModelTest.class);
	}

	public LinkedHashModelTest(String name) {
		super(name);
	}

	public Model makeEmptyModel() {
		return new LinkedHashModel();
	}
}
