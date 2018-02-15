package net.imglib2.algorithm.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.util.Intervals;
import net.imglib2.util.Pair;

public class GridsTest
{

	private final Interval interval = new FinalInterval( 3, 4, 5 );

	private final int[] blockSize = new int[] { 2, 2, 5 };

	@Test
	public void test()
	{

		final Interval interval = this.interval;

		final long[][] blockMinima = {
				{ 0, 0, 0 },
				{ 2, 0, 0 },
				{ 0, 2, 0 },
				{ 2, 2, 0 }
		};

		final List< long[] > blocks = Grids.collectAllOffsets( Intervals.dimensionsAsLongArray( interval ), blockSize );
		Assert.assertEquals( blockMinima.length, blocks.size() );
		for ( int i = 0; i < blockMinima.length; ++i )
			Assert.assertArrayEquals( blockMinima[ i ], blocks.get( i ) );

		final long[][] gridPositions = {
				{ 0, 0, 0 },
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ 1, 1, 0 }
		};

		final Interval[] intervals = {
				new FinalInterval( new long[] { 0, 0, 0 }, new long[] { 1, 1, 4 } ),
				new FinalInterval( new long[] { 2, 0, 0 }, new long[] { 2, 1, 4 } ),
				new FinalInterval( new long[] { 0, 2, 0 }, new long[] { 1, 3, 4 } ),
				new FinalInterval( new long[] { 2, 2, 0 }, new long[] { 2, 3, 4 } )
		};

		final List< Pair< Interval, long[] > > blocksAndGridPositions = Grids.collectAllContainedIntervalsWithGridPositions( Intervals.dimensionsAsLongArray( interval ), blockSize );
		Assert.assertEquals( intervals.length, blocksAndGridPositions.size() );
		for ( int i = 0; i < intervals.length; ++i )
		{
			final Interval block = blocksAndGridPositions.get( i ).getA();
			// if both contain each other they are equal
			Assert.assertTrue( Intervals.contains( block, intervals[ i ] ) && Intervals.contains( intervals[ i ], block ) );

			final long[] position = blocksAndGridPositions.get( i ).getB();
			Assert.assertArrayEquals( gridPositions[ i ], position );
		}
	}

	@Test
	public void testWithOffset()
	{

		final long[] offset = { 101, 37, -13 };
		final Interval interval = Intervals.translate( Intervals.translate( Intervals.translate( this.interval, offset[ 0 ], 0 ), offset[ 1 ], 1 ), offset[ 2 ], 2 );

		final long[][] blockMinima = {
				{ offset[ 0 ] + 0, offset[ 1 ] + 0, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 2, offset[ 1 ] + 0, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 0, offset[ 1 ] + 2, offset[ 2 ] + 0 },
				{ offset[ 0 ] + 2, offset[ 1 ] + 2, offset[ 2 ] + 0 },
		};

		final List< long[] > blocks = Grids.collectAllOffsets( Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize );
		Assert.assertEquals( blockMinima.length, blocks.size() );
		for ( int i = 0; i < blockMinima.length; ++i )
			Assert.assertArrayEquals( blockMinima[ i ], blocks.get( i ) );

		final long[][] gridPositions = {
				{ 0, 0, 0 },
				{ 1, 0, 0 },
				{ 0, 1, 0 },
				{ 1, 1, 0 }
		};

		final Interval[] intervals = {
				new FinalInterval( blockMinima[ 0 ], add( blockMinima[ 0 ], new long[] { 1, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 1 ], add( blockMinima[ 1 ], new long[] { 0, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 2 ], add( blockMinima[ 2 ], new long[] { 1, 1, 4 } ) ),
				new FinalInterval( blockMinima[ 3 ], add( blockMinima[ 3 ], new long[] { 0, 1, 4 } ) )
		};

		final List< Pair< Interval, long[] > > blocksAndGridPositions = Grids.collectAllContainedIntervalsWithGridPositions( Intervals.minAsLongArray( interval ), Intervals.maxAsLongArray( interval ), blockSize );
		Assert.assertEquals( intervals.length, blocksAndGridPositions.size() );
		for ( int i = 0; i < intervals.length; ++i )
		{
			final Interval block = blocksAndGridPositions.get( i ).getA();
			// if both contain each other they are equal
			Assert.assertTrue( Intervals.contains( block, intervals[ i ] ) && Intervals.contains( intervals[ i ], block ) );

			final long[] position = blocksAndGridPositions.get( i ).getB();
			Assert.assertArrayEquals( gridPositions[ i ], position );
		}
	}

	private static long[] add( final long[] arr, final long[] add )
	{
		final long[] result = new long[ arr.length ];
		for ( int d = 0; d < result.length; ++d )
			result[ d ] = arr[ d ] + add[ d ];
		return result;
	}

}
