// CoalescentTree.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.coalescent;

import pal.tree.*;

/**
 * interface defining a parameterized tree that
 * includes demographic information.
 *
 * @author Alexei Drummond
 */
public interface CoalescentTree {

	CoalescentIntervals getCoalescentIntervals();
}


