package net.imglib2.img;

import icy.sequence.Sequence;
import net.imglib2.img.basictypeaccess.array.ByteArray;
import net.imglib2.img.basictypeaccess.array.DoubleArray;
import net.imglib2.img.basictypeaccess.array.FloatArray;
import net.imglib2.img.basictypeaccess.array.IntArray;
import net.imglib2.img.basictypeaccess.array.ShortArray;
import net.imglib2.img.planar.PlanarImg;
import net.imglib2.type.numeric.integer.ByteType;
import net.imglib2.type.numeric.integer.IntType;
import net.imglib2.type.numeric.integer.ShortType;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedIntType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.DoubleType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Fraction;

public class IcySequenceAdapter
{

	private static final long[] getSqueezedDims( final Sequence sequence )
	{
		final int sizeX = sequence.getSizeX();
		final int sizeY = sequence.getSizeY();
		final int sizeC = sequence.getSizeC();
		final int sizeZ = sequence.getSizeZ();
		final int sizeT = sequence.getSizeT();

		/*
		 * We decide on the following dimension ordering.
		 */
		final long[] oDims = new long[] { sizeX, sizeY, sizeC, sizeZ, sizeT };
		final long[] dims = squeezeSingletonDims( oDims );
		return dims;
	}


	private static final long[] squeezeSingletonDims( final long[] originalDims )
	{
		int squeeze = 0;
		for ( final long l : originalDims )
		{
			if ( l <= 1 )
			{
				squeeze++;
			}
		}
		if ( squeeze == 0 ) { return originalDims; }

		final long[] dims = new long[ originalDims.length - squeeze ];
		int index = 0;
		for ( final long l : dims )
		{
			if ( l > 1 )
			{
				dims[ index++ ] = l;
			}
		}
		return dims;
	}

	/**
	 * Returns the spatial and temporal calibration of the specified sequence in
	 * a <code>double</code> array.
	 * <ol start ="0">
	 * <li>dx in µm.
	 * <li>dy in µm.
	 * <li>dz in µm.
	 * <li>dt in s.
	 * </ol>
	 * 
	 * @param sequence
	 * @return a new, 4-elements, <code>double</code> array.
	 */
	public static final double[] getCalibration( final Sequence sequence )
	{
		final double[] calibration = new double[ 4 ]; // XYZT

		calibration[ 0 ] = sequence.getPixelSizeX();
		calibration[ 1 ] = sequence.getPixelSizeY();
		calibration[ 2 ] = sequence.getPixelSizeZ();
		calibration[ 3 ] = sequence.getTimeInterval();
		return calibration;

	}

	public static final PlanarImg< ?, ? > wrap( final Sequence sequence )
	{
		switch ( sequence.getDataType_() )
		{
		case BYTE:
			return wrapByte( sequence );
		case INT:
			return wrapInt( sequence );
		case SHORT:
			return wrapShort( sequence );
		case UBYTE:
			return wrapUnsignedByte( sequence );
		case UINT:
			return wrapUnsignedInt( sequence );
		case USHORT:
			return wrapUnsignedShort( sequence );
		case DOUBLE:
			return wrapDouble( sequence );
		case FLOAT:
			return wrapFloat( sequence );
		case LONG:
		case ULONG:
		default:
			throw new RuntimeException( "Only 8, 16, float or double supported!" );
		}
	}

	public static PlanarImg< ByteType, ByteArray > wrapByte( final Sequence sequence )
	{
		final PlanarImg< ByteType, ByteArray > img = new PlanarImg< ByteType, ByteArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final byte[] data = sequence.getDataXYAsByte( t, z, c );
					final ByteArray plane = new ByteArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static PlanarImg< DoubleType, DoubleArray > wrapDouble( final Sequence sequence )
	{
		final PlanarImg< DoubleType, DoubleArray > img = new PlanarImg< DoubleType, DoubleArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final double[] data = sequence.getDataXYAsDouble( t, z, c );
					final DoubleArray plane = new DoubleArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static final PlanarImg< FloatType, FloatArray > wrapFloat( final Sequence sequence )
	{
		final PlanarImg< FloatType, FloatArray > img = new PlanarImg< FloatType, FloatArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final float[] data = sequence.getDataXYAsFloat( t, z, c );
					final FloatArray plane = new FloatArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}


	public static PlanarImg< IntType, IntArray > wrapInt( final Sequence sequence )
	{
		final PlanarImg< IntType, IntArray > img = new PlanarImg< IntType, IntArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final int[] data = sequence.getDataXYAsInt( t, z, c );
					final IntArray plane = new IntArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static PlanarImg< ShortType, ShortArray > wrapShort( final Sequence sequence )
	{
		final PlanarImg< ShortType, ShortArray > img = new PlanarImg< ShortType, ShortArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final short[] data = sequence.getDataXYAsShort( t, z, c );
					final ShortArray plane = new ShortArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static final PlanarImg< UnsignedByteType, ByteArray > wrapUnsignedByte( final Sequence sequence )
	{
		final PlanarImg< UnsignedByteType, ByteArray > img = new PlanarImg< UnsignedByteType, ByteArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final byte[] data = sequence.getDataXYAsByte( t, z, c );
					final ByteArray plane = new ByteArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static PlanarImg< UnsignedIntType, IntArray > wrapUnsignedInt( final Sequence sequence )
	{
		final PlanarImg< UnsignedIntType, IntArray > img = new PlanarImg< UnsignedIntType, IntArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final int[] data = sequence.getDataXYAsInt( t, z, c );
					final IntArray plane = new IntArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	public static PlanarImg< UnsignedShortType, ShortArray > wrapUnsignedShort( final Sequence sequence )
	{
		final PlanarImg< UnsignedShortType, ShortArray > img = new PlanarImg< UnsignedShortType, ShortArray >( getSqueezedDims( sequence ), new Fraction() );
		int no = 0;
		for ( int t = 0; t < sequence.getSizeT(); t++ )
		{
			for ( int z = 0; z < sequence.getSizeZ(); z++ )
			{
				for ( int c = 0; c < sequence.getSizeC(); c++ )
				{
					final short[] data = sequence.getDataXYAsShort( t, z, c );
					final ShortArray plane = new ShortArray( data );
					img.setPlane( no++, plane );
				}
			}
		}
		return img;
	}

	private IcySequenceAdapter()
	{}
}
