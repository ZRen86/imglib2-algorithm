package net.imglib2.algorithm.convolution;

import net.imglib2.Dimensions;
import net.imglib2.Interval;
import net.imglib2.RandomAccessible;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;
import net.imglib2.util.Util;
import net.imglib2.util.ValuePair;
import net.imglib2.view.Views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Helper to implement {@link Convolution#concat}.
 *
 * @author Matthias Arzt
 */
class Concatenation< T > implements Convolution< T >
{

	private final List< Convolution< T > > steps;

	Concatenation( List< ? extends Convolution< T > > steps )
	{
		this.steps = new ArrayList<>( steps );
	}

	@Override public void setExecutor( ExecutorService executor )
	{
		steps.forEach( step -> step.setExecutor( executor ) );
	}

	@Override
	public Interval requiredSourceInterval( Interval targetInterval )
	{
		Interval result = targetInterval;
		for ( int i = steps.size() - 1; i >= 0; i-- )
			result = steps.get( i ).requiredSourceInterval( result );
		return result;
	}

	@Override
	public T preferredSourceType( T targetType )
	{
		for ( int i = steps.size() - 1; i >= 0; i-- )
			targetType = steps.get( i ).preferredSourceType( targetType );
		return targetType;
	}

	@Override
	public void process( RandomAccessible< ? extends T > source, RandomAccessibleInterval< ? extends T > target )
	{
		List< Pair< T, Interval > > srcIntervals = tmpIntervals( Util.getTypeFromInterval( target ), target );
		RandomAccessibleInterval< ? extends T > currentSource = Views.interval( source, srcIntervals.get( 0 ).getB() );
		RandomAccessibleInterval< ? extends T > available = null;

		for ( int i = 0; i < steps.size(); i++ )
		{
			Convolution< T > step = steps.get( i );
			T targetType = srcIntervals.get( i + 1 ).getA();
			Interval targetInterval = srcIntervals.get( i + 1 ).getB();
			RandomAccessibleInterval< ? extends T > currentTarget =
					( i == steps.size() - 1 ) ? target : null;

			if ( currentTarget == null && available != null &&
					Intervals.contains( available, targetInterval ) &&
					Util.getTypeFromInterval( available ).getClass().equals( targetType.getClass() ) )
				currentTarget = Views.interval( available, targetInterval );

			if ( currentTarget == null )
				currentTarget = createImage( uncheckedCast( targetType ), targetInterval );

			step.process( currentSource, currentTarget );

			if ( i > 0 )
				available = currentSource;
			currentSource = currentTarget;
		}
	}

	private static < T extends NativeType< T > > RandomAccessibleInterval< T > createImage( T targetType, Interval targetInterval )
	{
		long[] dimensions = Intervals.dimensionsAsLongArray( targetInterval );
		Img< T > ts = getImgFactory( targetInterval, targetType ).create( dimensions );
		return Views.translate( ts, Intervals.minAsLongArray( targetInterval ) );
	}

	private static < T extends NativeType< T > > ImgFactory< T > getImgFactory( final Dimensions targetsize, final T type )
	{
		if ( canUseArrayImgFactory( targetsize ) )
			return new ArrayImgFactory<>( type );
		final int cellSize = ( int ) Math.pow( Integer.MAX_VALUE / type.getEntitiesPerPixel().getRatio(), 1.0 / targetsize.numDimensions() );
		return new CellImgFactory<>( type, cellSize );
	}

	private static boolean canUseArrayImgFactory( final Dimensions targetsize )
	{
		return Intervals.numElements( targetsize ) <= Integer.MAX_VALUE;
	}

	private static < T > T uncheckedCast( Object in )
	{
		@SuppressWarnings( "unchecked" )
		T in1 = ( T ) in;
		return in1;
	}

	private List< Pair< T, Interval > > tmpIntervals( T type, Interval interval )
	{
		List< Pair< T, Interval > > result = new ArrayList<>( Collections.nCopies( steps.size() + 1, null ) );
		result.set( steps.size(), new ValuePair<>( type, interval ) );
		for ( int i = steps.size() - 1; i >= 0; i-- )
		{
			Convolution< T > step = steps.get( i );
			interval = step.requiredSourceInterval( interval );
			type = step.preferredSourceType( type );
			result.set( i, new ValuePair<>( type, interval ) );
		}
		return result;
	}
}
