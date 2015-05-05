package plugins.tinevez.imglib2icy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import net.imglib2.Interval;
import net.imglib2.img.Img;
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

public class ImgLib2IcyFunctions
{
	public static < T extends NumericType< T > & RealType< T >> Sequence wrap( final Img< T > img )
	{
		// Awesome heuristics.
		final DimensionArrangement arrangement = heuristics( img );
		return wrap( img, arrangement );
	}

	private static DimensionArrangement heuristics( final Interval source )
	{
		final int n = source.numDimensions();
		if ( n <= 2 )
		{
			return DimensionArrangement.XY;
		}
		else if ( n == 3 )
		{
			// C, Z or T?
			final long l = source.dimension( 2 );
			if ( l <= 5 )
			{
				return DimensionArrangement.XYC;
			}
			else if ( l <= 41 )
			{
				return DimensionArrangement.XYZ;
			}
			else
			{
				return DimensionArrangement.XYT;
			}
		}
		else if ( n == 4 )
		{
			// CZ, CT or ZT?
			final long l1 = source.dimension( 2 );
			if ( l1 <= 5 )
			{
				final long l2 = source.dimension( 3 );
				if ( l2 <= 41 )
				{
					return DimensionArrangement.XYCZ;
				}
				else
				{
					return DimensionArrangement.XYCT;
				}
			}
			else
			{
				return DimensionArrangement.XYZT;
			}
		}
		else if ( n == 5 )
		{
			return DimensionArrangement.XYCZT;
		}
		else
		{
			throw new UnsupportedOperationException( "Source Img with ndims > 5 are not supported." );
		}
	}

	public static < T extends NumericType< T > & RealType< T >> Sequence wrap( final Img< T > img, final DimensionArrangement arrangement )
	{
		return new VirtualSequence( img, arrangement );
	}

	public static < T extends NumericType< T > & RealType< T >> Img< T > wrap( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrap( sequence );
	}

	public static < T extends NumericType< T > & RealType< T >> Img< T > wrap( final IcyBufferedImage image )
	{
		return wrap( new Sequence( image ) );
	}

	public static Img< ByteType > wrapByte( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapByte( sequence );
	}

	public static Img< DoubleType > wrapDouble( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapDouble( sequence );
	}

	public static Img< FloatType > wrapFloat( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapFloat( sequence );
	}

	public static Img< IntType > wrapInt( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapInt( sequence );
	}

	public static Img< ShortType > wrapShort( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapShort( sequence );
	}

	public static Img< UnsignedByteType > wrapUnsignedByte( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapUnsignedByte( sequence );
	}

	public static Img< UnsignedIntType > wrapUnsignedInt( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapUnsignedInt( sequence );
	}

	public static Img< UnsignedShortType > wrapUnsignedShort( final Sequence sequence )
	{
		return ImgLib2IcySequenceAdapter.wrapUnsignedShort( sequence );
	}

	public static DimensionArrangement getDimensionArrangement( final Sequence sequence )
	{
		final int sizeC = sequence.getSizeC();
		final int sizeZ = sequence.getSizeZ();
		final int sizeT = sequence.getSizeT();

		final boolean hasC = sizeC > 1;
		final boolean hasZ = sizeZ > 1;
		final boolean hasT = sizeT > 1;

		if ( hasC )
		{
			if ( hasZ )
			{
				if ( hasT )
				{
					return DimensionArrangement.XYCZT;
				}
				else
				{
					return DimensionArrangement.XYCZ;
				}
			}
			else
			{
				// No Z.
				if ( hasT )
				{
					return DimensionArrangement.XYCT;
				}
				else
				{
					return DimensionArrangement.XYC;
				}
			}
		}
		else
		{
			// No C.
			if ( hasZ )
			{
				if ( hasT )
				{
					return DimensionArrangement.XYZT;
				}
				else
				{
					return DimensionArrangement.XYZ;
				}
			}
			else
			{
				// No Z.
				if ( hasT )
				{
					return DimensionArrangement.XYT;
				}
				else
				{
					return DimensionArrangement.XY;
				}
			}
		}
	}

	private ImgLib2IcyFunctions()
	{}

}
