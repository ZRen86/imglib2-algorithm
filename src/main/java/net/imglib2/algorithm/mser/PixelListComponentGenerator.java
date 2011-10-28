package net.imglib2.algorithm.mser;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.LongType;

public class PixelListComponentGenerator< T extends Type< T > > implements Component.Generator< T, PixelListComponent< T > >
{
	final T maxValue;
	
	final long[] dimensions;
	
	final Img< LongType > linkedList;

	public PixelListComponentGenerator( final T maxValue, final RandomAccessibleInterval< T > input, final ImgFactory< LongType > imgFactory )
	{
		this.maxValue = maxValue;
		dimensions = new long[ input.numDimensions() ];
		input.dimensions( dimensions );
		linkedList = imgFactory.create( dimensions, new LongType() );
	}
	
	@Override
	public PixelListComponent< T > createComponent( T value )
	{
		return new PixelListComponent< T >( value, this );
	}

	@Override
	public PixelListComponent< T > createMaxComponent()
	{
		return new PixelListComponent< T >( maxValue, this );
	}
}
