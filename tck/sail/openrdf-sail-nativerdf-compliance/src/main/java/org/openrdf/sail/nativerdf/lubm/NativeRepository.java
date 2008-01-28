/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.nativerdf.lubm;

import java.io.File;

import org.openrdf.sail.Sail;
import org.openrdf.sail.lubm.LUBMRepository;
import org.openrdf.sail.nativerdf.NativeStore;


/**
 * 
 */
public class NativeRepository extends LUBMRepository {

	// implements LUBMRepository.createSail(...)
	@Override
	public Sail createSail(String database) {
		File dataDir = new File(database);
		return new NativeStore(dataDir, "spoc,posc");
	}

}
