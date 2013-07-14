/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.swing;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.SpringLayout;

public class SpringLayoutSize extends SpringLayout
{
	Dimension dimension;
	DimensionCalculator dimensionCalculator;
	int depth;

	public SpringLayoutSize (int depth, Dimension dimension, DimensionCalculator dimensionCalculator)
	{
		this.dimension = dimension;
		this.depth = depth;
		this.dimensionCalculator = dimensionCalculator;
	}
	
	public SpringLayoutSize (int depth, Dimension dimension)
	{
		this(depth, dimension, null);
	}
	
	public Container getNestedParent(Container parent)
	{
		for (int i=1; i<depth; ++i)
			parent = parent.getParent();
		
		return parent;
	}

	public Dimension calculateDimension (Container parent)
	{		
		Container nestedParent = getNestedParent (parent);

		Dimension result =  
			new Dimension (
				dimension.width > 0 ? dimension.width : nestedParent.getSize().width,
				dimension.height > 0 ? dimension.height : nestedParent.getSize().height
			);
		
		if (dimensionCalculator != null)
			result = dimensionCalculator.calculate(result);
			
		return result;
	}
	
	@Override
	public Dimension maximumLayoutSize(Container parent) 
	{
		return calculateDimension(parent);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) 
	{
		return calculateDimension(parent);
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) 
	{
		return calculateDimension(parent);
	}
}
