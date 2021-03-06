// TreePainter.java
//
// (c) 1999-2001 PAL Development Core Team
//
// This package may be distributed under the
// terms of the Lesser GNU General Public License (LGPL)

package pal.gui;

import pal.tree.*;
import pal.io.*;
import pal.misc.*;

import headless.awt.*;

/**
 * A class that can paint a tree (in a circular fashion) into a Graphics object .
 *
 * @version $Id: TreePainterCircular.java,v 1.8 2001/07/13 14:39:13 korbinian Exp $
 *
 * @author Alexei Drummond, Matthew Goode
 */
public class TreePainterCircular extends TreePainter {

	public static final int RIGHTBORDER = 75;
	public static final int LEFTBORDER = 10;
	public static final int TOPBORDER = 20;
	public static final int BOTTOMBORDER = 30;
	static final int SYMBOL_SIZE = 8;
	public static final int FONT_SIZE = 9;
	public static final int YSPACER = 20;
	public static final int XSPACER = 10;

	double maxAngle_, maxRadius_;
  public TreePainterCircular(Tree toDisplay, String title, boolean showTitle) {
		this(toDisplay, title, showTitle,null);
	}
	public TreePainterCircular(Tree toDisplay, String title, boolean showTitle, Node highlightNode) {
		super(toDisplay, title, showTitle);
		standardTreePrep(highlightNode);
		maxRadius_ = treeNode.getNodeHeight();
		maxAngle_ = NodeUtils.getLeafCount(treeNode);
	}
	
	/**
	 * Returns the preferred size for drawing
	 * (that is the size that will show everything nicely)
	 */
	public Dimension getPreferredSize() {
		return new Dimension(100 + LEFTBORDER + RIGHTBORDER, 
			100+TOPBORDER+ BOTTOMBORDER);
	}

	protected void paint(PositionedNode node, CircularGraphics cg) {
		cg.setColor(FOREGROUND);
		double angle = node.x;
		double radius = node.getNodeHeight();

		if (node.hasChildren()) {
			for (int i = 0; i < node.getChildCount(); i++) {
				paintLeafBranch(node, (PositionedNode)node.getChild(i), cg);
			}

			for (int i = 0; i < node.getChildCount(); i++) {
				paint((PositionedNode)node.getChild(i), cg);
			}
		} else {

			if ((maxLeafTime > 0.0) && isUsingColor()) {
				cg.setColor(Color.getHSBColor((float)(maxLeafTime - radius)/(float)maxLeafTime, 1.0f, 1.0f));
			} else {
				cg.setColor(NORMAL_LABEL_COLOR);
			}


			if (isUsingColor()) {
				cg.fillPoint(angle,radius,2);
			}
			if (isUsingSymbols() && (getTimeOrderCharacterData() != null)) {
				cg.drawSymbol(angle,radius, XSPACER, SYMBOL_SIZE,	getTimeOrderCharacterData().getTimeOrdinal(getTimeOrderCharacterData().whichIdNumber(node.getIdentifier().getName())));
			} else {
        if(isUsingColor()) {
					cg.drawString( node.getIdentifier().getName(), angle,radius, XSPACER);
					if(node.isHighlighted()) {
						cg.setColor(Color.red);
						cg.circleString(node.getIdentifier().getName(), angle,radius, XSPACER);
					}
				} else {
					if(node.isHighlighted()) {
						cg.setColor(Color.red);
					}
					cg.drawString( node.getIdentifier().getName(), angle,radius, XSPACER);
				}

			}
		}

	}


	private void paintLeafBranch(PositionedNode parentNode, PositionedNode childNode, CircularGraphics g) {

		// paint join to parent
		g.drawArc(parentNode.x,childNode.x, parentNode.getNodeHeight()	);
		// paint branch
		g.drawLineDegreeAlign(childNode.x, childNode.getNodeHeight(), parentNode.getNodeHeight()
							);

		if (isShowingNodeHeights()) {

			String label = FormattedOutput.getInstance().getDecimalString(childNode.getNodeHeight(), 4);
			g.drawString(label, childNode.x, childNode.getNodeHeight(), XSPACER);
		}
	}

	public void paint(Graphics g, int displayWidth, int displayHeight) {
		paint(g,displayWidth,displayHeight,false);
	}
	public void paint(Graphics g, int displayWidth, int displayHeight,boolean invert) {

		CircularGraphics cg =
			new CircularGraphics(g,maxAngle_,maxRadius_,
				LEFTBORDER,TOPBORDER,
				(displayWidth - LEFTBORDER - RIGHTBORDER),
				(displayHeight - TOPBORDER - BOTTOMBORDER),
				invert
			);

		cg.setFont(new Font("Times", Font.PLAIN, FONT_SIZE));

		g.setColor(BACKGROUND);
		g.fillRect(0, 0, displayWidth, displayHeight);
		paint(treeNode, cg);

		doTitle(g,LEFTBORDER, TOPBORDER - 8);
		//doScale(g,1,LEFTBORDER,displayHeight - BOTTOMBORDER + 12);

	}
}



