
package plugins.tinevez.imglib2icy;

import icy.sequence.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2IcySplitSequenceAdapter
{
	private static final long[] getDims( final Sequence sequence , final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final int sizeX = sequence.getSizeX();
		final int sizeY = sequence.getSizeY();

		final int sizeC;
		if ( splitC )
		{
			sizeC = 0;
		}
		else
		{
			sizeC = sequence.getSizeC();
		}

		final int sizeZ;
		if ( splitZ )
		{
			sizeZ = 0;
		}
		else
		{
			sizeZ = sequence.getSizeZ();
		}

		final int sizeT;
		if ( splitT )
		{
			sizeT = 0;
		}
		else
		{
			sizeT = sequence.getSizeT();

		}
		/*
		 * We decide on the following dimension ordering.
		 */
		return new long[] { sizeX, sizeY, sizeC, sizeZ, sizeT };
	}

	private static final long[] getSqueezedDims( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final long[] dims = squeezeSingletonDims( getDims( sequence, splitC, splitZ, splitT ) );
		return dims;
	}

	private static final long[] squeezeSingletonDims( final long[] originalDims )
	{
		final long[] dims = new long[ originalDims.length ];
		int index = 0;
		for ( final long l : originalDims )
		{
			if ( l <= 1 )
			{
				continue;
			}
			dims[ index++ ] = l;
		}
		return Arrays.copyOf( dims, index );
	}

	private static final int linearIndexFromCoordinate(
			final int c, final int z, final int t,
			final int sizeC, final int sizeZ,
			final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final int A;
		final int B;
		final int C;
		if ( splitC )
		{
			if ( splitZ )
			{
				A = 1;
				B = sizeC;
				if ( splitT )
				{
					// Split everything
					C = sizeC * sizeZ;
				}
				else
				{
					C = 0;
				}
			}
			else
			{
				A = 1;
				// Aggregate Z
				if ( splitT )
				{
					B = 0;
					C = sizeC;
				}
				else
				{
					// Aggregate Z & T
					B = 0;
					C = 0;
				}
			}
		}
		else
		{
			// Aggregate C.
			A = 0;
			if ( splitZ )
			{
				B = 1;
				if ( splitT )
				{
					C = sizeZ;
				}
				else
				{
					// Aggregate C & T
					C = 0;
				}
			}
			else
			{
				B = 0;
				if ( splitT )
				{
					// Aggregate C & Z only
					C = 1;
				}
				else
				{
					// Aggregate everything.
					C = 0;
				}
			}
		}
		return A * c + B * z + C * t;
	}

	public String hi()
	{
		return "Hi!";
	}

	public static DimensionArrangement getDimensionArrangement( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		DimensionArrangement da = ImgLib2IcyFunctions.getDimensionArrangement( sequence );
		if ( splitC )
		{
			da = da.dropC();
		}
		if ( splitZ )
		{
			da = da.dropZ();
		}
		if ( splitT )
		{
			da = da.dropT();
		}
		return da;
	}

	@SuppressWarnings( "unchecked" )
	public static final List< Img< UnsignedByteType >> wrapUnsignedByte( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< UnsignedByteType >> imgs = new ArrayList< Img< UnsignedByteType > >();
		final List< Integer > planeCounters = new ArrayList< Integer >();

		final int sizeC = sequence.getSizeC();
		final int sizeZ = sequence.getSizeZ();
		final int sizeT = sequence.getSizeT();

		for ( int t = 0; t < sizeT; t++ )
		{
			for ( int z = 0; z < sizeZ; z++ )
			{
				for ( int c = 0; c < sizeC; c++ )
				{
					final int index = linearIndexFromCoordinate( c, z, t,
							sizeC, sizeZ,
							splitC, splitZ, splitT );

					final PlanarImg< UnsignedByteType, ByteArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.unsignedBytes( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< UnsignedByteType, ByteArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final byte[] data = sequence.getDataXYAsByte( t, z, c );
					final ByteArray plane = new ByteArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
	}

	private ImgLib2IcySplitSequenceAdapter()
	{}
}
