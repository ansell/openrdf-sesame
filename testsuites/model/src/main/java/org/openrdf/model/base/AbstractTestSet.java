/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.model.base;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * Abstract test class for {@link Set} methods and contracts.
 * <p>
 * Since {@link Set} doesn't stipulate much new behavior that isn't already
 * found in {@link Collection}, this class basically just adds tests for
 * {@link Set#equals} and {@link Set#hashCode()} along with an updated
 * {@link #verify()} that ensures elements do not appear more than once in the
 * set.
 * <p>
 * To use, subclass and override the {@link #makeEmptySet()}
 * method.  You may have to override other protected methods if your
 * set is not modifiable, or if your set restricts what kinds of
 * elements may be added; see {@link AbstractTestCollection} for more details.
 *
 * @since Commons Collections 3.0
 * @version $Revision: 646780 $ $Date: 2008-04-10 13:48:07 +0100 (Thu, 10 Apr 2008) $
 * 
 * @author Paul Jack
 */
public abstract class AbstractTestSet extends AbstractTestCollection {

    /**
     * JUnit constructor.
     *
     * @param name  name for test
     */
    public AbstractTestSet(String name) {
        super(name);
    }

    //-----------------------------------------------------------------------
    /**
     * Provides additional verifications for sets.
     */
    public void verify() {
        super.verify();
        
        assertEquals("Sets should be equal", confirmed, collection);
        assertEquals("Sets should have equal hashCodes", 
                     confirmed.hashCode(), collection.hashCode());
        Collection set = makeConfirmedCollection();
        Iterator iterator = collection.iterator();
        while (iterator.hasNext()) {
            assertTrue("Set.iterator should only return unique elements", 
                       set.add(iterator.next()));
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Set equals method is defined.
     */
    public boolean isEqualsCheckable() {
        return true;
    }

    /**
     * Returns an empty Set for use in modification testing.
     *
     * @return a confirmed empty collection
     */
    public Collection makeConfirmedCollection() {
        return new HashSet();
    }

    /**
     * Returns a full Set for use in modification testing.
     *
     * @return a confirmed full collection
     */
    public Collection makeConfirmedFullCollection() {
        Collection set = makeConfirmedCollection();
        set.addAll(Arrays.asList(getFullElements()));
        return set;
    }

    /**
     * Makes an empty set.  The returned set should have no elements.
     *
     * @return an empty set
     */
    public abstract Set makeEmptySet();

    /**
     * Makes a full set by first creating an empty set and then adding
     * all the elements returned by {@link #getFullElements()}.
     *
     * Override if your set does not support the add operation.
     *
     * @return a full set
     */
    public Set makeFullSet() {
        Set set = makeEmptySet();
        set.addAll(Arrays.asList(getFullElements()));
        return set;
    }

    /**
     * Makes an empty collection by invoking {@link #makeEmptySet()}.  
     *
     * @return an empty collection
     */
    public final Collection makeCollection() {
        return makeEmptySet();
    }

    /**
     * Makes a full collection by invoking {@link #makeFullSet()}.
     *
     * @return a full collection
     */
    public final Collection makeFullCollection() {
        return makeFullSet();
    }

    //-----------------------------------------------------------------------
    /**
     * Return the {@link AbstractTestCollection#collection} fixture, but cast as a Set.  
     */
    public Set getSet() {
        return (Set)collection;
    }

    /**
     * Return the {@link AbstractTestCollection#confirmed} fixture, but cast as a Set.
     */
    public Set getConfirmedSet() {
        return (Set)confirmed;
    }

    //-----------------------------------------------------------------------
    /**
     * Tests {@link Set#equals(Object)}.
     */
    public void testSetEquals() {
        resetEmpty();
        assertEquals("Empty sets should be equal", 
                     getSet(), getConfirmedSet());
        verify();

        Collection set2 = makeConfirmedCollection();
        set2.add("foo");
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

    /**
     * Tests {@link Set#hashCode()}.
     */
    public void testSetHashCode() {
        resetEmpty();
        assertEquals("Empty sets have equal hashCodes", 
                     getSet().hashCode(), getConfirmedSet().hashCode());

        resetFull();
        assertEquals("Equal sets have equal hashCodes", 
                     getSet().hashCode(), getConfirmedSet().hashCode());
    }

}
