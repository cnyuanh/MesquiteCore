// Parameterized.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)


package pal.misc;

import java.io.*;


/**
 * interface for class with (optimizable) parameters
 *
 * @version $Id: Parameterized.java,v 1.3 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Korbinian Strimmer
 */
public interface Parameterized
{
	/**
	 * get number of parameters
	 *
	 * @return number of parameters
	 */
	int getNumParameters();

	/**
	 * set model parameter
	 *
	 * @param param  parameter value
	 * @param n  parameter number
	 */
	void setParameter(double param, int n);

	/**
	 * get model parameter
	 *
	 * @param n  parameter number
	 *
	 * @return parameter value
	 */
	double getParameter(int n);

	
	/**
	 * set standard errors for model parameter
	 *
	 * @param paramSE  standard error of parameter value
	 * @param n parameter number
	 */
	void setParameterSE(double paramSE, int n);

	
	/**
	 * get lower parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return lower bound
	 */
	double getLowerLimit(int n);

	/**
	 * get upper parameter limit
	 *
	 * @param n parameter number
	 *
	 * @return upper bound
	 */
	double getUpperLimit(int n);


	/**
	 * get default value of parameter
	 *
	 * @param n parameter number
	 *
	 * @return default value
	 */
	double getDefaultValue(int n);
}

