
package plugins.tinevez.imglib2icy;

import icy.sequence.Sequence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.img.planar.PlanarImgs;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2IcySplitSequenceAdapter
{
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

	@SuppressWarnings( { "unchecked", "rawtypes" } )
	public static final < T extends NumericType< T > & RealType< T >> List< Img< T >> wrap( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		switch ( sequence.getDataType_() )
		{
		case BYTE:
			return ( List ) wrapByte( sequence, splitC, splitZ, splitT );
		case INT:
			return ( List ) wrapInt( sequence, splitC, splitZ, splitT );
		case SHORT:
			return ( List ) wrapShort( sequence, splitC, splitZ, splitT );
		case UBYTE:
			return ( List ) wrapUnsignedByte( sequence, splitC, splitZ, splitT );
		case UINT:
			return ( List ) wrapUnsignedInt( sequence, splitC, splitZ, splitT );
		case USHORT:
			return ( List ) wrapUnsignedShort( sequence, splitC, splitZ, splitT );
		case DOUBLE:
			return ( List ) wrapDouble( sequence, splitC, splitZ, splitT );
		case FLOAT:
			return ( List ) wrapFloat( sequence, splitC, splitZ, splitT );
		case LONG:
		case ULONG:
		default:
			throw new RuntimeException( "Only byte, int, short, float or double supported!" );
		}
	}

	@SuppressWarnings( "unchecked" )
	public static List< Img< ByteType >> wrapByte( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< ByteType >> imgs = new ArrayList< Img< ByteType > >();
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

					final PlanarImg< ByteType, ByteArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.bytes( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< ByteType, ByteArray > ) imgs.get( index );
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

	@SuppressWarnings( "unchecked" )
	public static List< Img< DoubleType >> wrapDouble( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< DoubleType >> imgs = new ArrayList< Img< DoubleType > >();
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

					final PlanarImg< DoubleType, DoubleArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.doubles( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< DoubleType, DoubleArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final double[] data = sequence.getDataXYAsDouble( t, z, c );
					final DoubleArray plane = new DoubleArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
	}

	@SuppressWarnings( "unchecked" )
	public static final List< Img< FloatType >> wrapFloat( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< FloatType >> imgs = new ArrayList< Img< FloatType > >();
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

					final PlanarImg< FloatType, FloatArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.floats( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< FloatType, FloatArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final float[] data = sequence.getDataXYAsFloat(  t, z, c );
					final FloatArray plane = new FloatArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
	}

	@SuppressWarnings( "unchecked" )
	public static List<Img< IntType >> wrapInt( final Sequence sequence , final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< IntType >> imgs = new ArrayList< Img< IntType > >();
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

					final PlanarImg< IntType, IntArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.ints( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< IntType, IntArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final int[] data = sequence.getDataXYAsInt(  t, z, c );
					final IntArray plane = new IntArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
	}

	@SuppressWarnings( "unchecked" )
	public static List< Img< ShortType >> wrapShort( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< ShortType >> imgs = new ArrayList< Img< ShortType > >();
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

					final PlanarImg< ShortType, ShortArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.shorts( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< ShortType, ShortArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final short[] data = sequence.getDataXYAsShort( t, z, c );
					final ShortArray plane = new ShortArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
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

	@SuppressWarnings( "unchecked" )
	public static List< Img< UnsignedIntType >> wrapUnsignedInt( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< UnsignedIntType >> imgs = new ArrayList< Img< UnsignedIntType > >();
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

					final PlanarImg< UnsignedIntType, IntArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.unsignedInts( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< UnsignedIntType, IntArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final int[] data = sequence.getDataXYAsInt( t, z, c );
					final IntArray plane = new IntArray( data );
					img.setPlane( count++, plane );
					planeCounters.set( index, count );
				}
			}
		}
		return imgs;
	}

	@SuppressWarnings( "unchecked" )
	public static List< Img< UnsignedShortType >> wrapUnsignedShort( final Sequence sequence, final boolean splitC, final boolean splitZ, final boolean splitT )
	{
		final List< Img< UnsignedShortType >> imgs = new ArrayList< Img< UnsignedShortType > >();
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

					final PlanarImg< UnsignedShortType, ShortArray > img;
					Integer count;
					if ( index >= imgs.size() )
					{
						img = PlanarImgs.unsignedShorts( getSqueezedDims( sequence, splitC, splitZ, splitT ) );
						imgs.add( img );

						count = 0;
						planeCounters.add( count );
					}
					else
					{
						img = ( PlanarImg< UnsignedShortType, ShortArray > ) imgs.get( index );
						count = planeCounters.get( index );
					}

					final short[] data = sequence.getDataXYAsShort( t, z, c );
					final ShortArray plane = new ShortArray( data );
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
