package plugins.tinevez.imglib2icy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.sequence.VolumetricImage;
import icy.type.DataType;

import java.util.ArrayList;
import java.util.TreeMap;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.converter.Converter;
import net.imglib2.converter.RealFloatConverter;
import net.imglib2.converter.RealUnsignedByteConverter;
import net.imglib2.converter.RealUnsignedShortConverter;
import net.imglib2.display.projector.IterableIntervalProjector2D;
import net.imglib2.img.array.ArrayImg;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.basictypeaccess.array.ArrayDataAccess;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Util;
import net.imglib2.view.Views;

public class VirtualSequence extends Sequence
{

	public static enum DimensionArrangement
	{
		XY( false, false, false, 0, 0, 0, 0, 0 ),
		XYC( true, false, false, 0, 0, 0, 0, 0 ),
		XYZ( false, true, false, 0, 2, 2, 0, 0 ),
		XYT( false, false, true, 0, 0, 0, 2, 2 ),
		XYCT( true, false, true, 2, 0, 0, 3, 2 ),
		XYCZ( true, true, false, 2, 3, 2, 0, 0 ),
		XYZT( false, true, true, 0, 2, 2, 3, 3 ),
		XYCZT( true, true, true, 2, 3, 2, 4, 3 );

		private final boolean hasZ;

		private final int dimZ;

		private final int targetDimZ;

		private final boolean hasT;

		private final int dimT;

		private final int targetDimT;

		private final boolean hasC;

		private final int dimC;

		private DimensionArrangement( final boolean hasC, final boolean hasZ, final boolean hasT, final int dimC, final int dimZ, final int targetDimZ, final int dimT, final int targetDimT )
		{
			this.hasC = hasC;
			this.hasZ = hasZ;
			this.hasT = hasT;
			this.dimC = dimC;
			this.dimZ = dimZ;
			this.targetDimZ = targetDimZ;
			this.dimT = dimT;
			this.targetDimT = targetDimT;
		}

		public int numDimensions()
		{
			int ndims = 2;
			if ( hasC )
				ndims++;
			if ( hasZ )
				ndims++;
			if ( hasT )
				ndims++;
			return ndims;
		}

		public DimensionArrangement dropC()
		{
			switch ( this )
			{
			case XYC:
				return XY;
			case XYCT:
				return XYT;
			case XYCZ:
				return XYZ;
			case XYCZT:
				return XYZT;
			default:
				return this;
			}
		}

		public DimensionArrangement dropZ()
		{
			switch ( this )
			{
			case XYZ:
				return XY;
			case XYCZ:
				return XYC;
			case XYZT:
				return XYT;
			case XYCZT:
				return XYCT;
			default:
				return this;
			}
		}

		public DimensionArrangement dropT()
		{
			switch ( this )
			{
			case XYT:
				return XY;
			case XYCT:
				return XYC;
			case XYZT:
				return XYT;
			case XYCZT:
				return XYCZ;
			default:
				return this;
			}
		}
	}

	private final IcyBufferedImage image;

	private final VolumetricImage volumetricImage;

	private final int minZ;

	private final int maxZ;

	private final int sizeZ;

	private final int sizeT;

	private int previousT = -1;

	private int previousZ = -1;

	private final int sizeX;

	private final int sizeY;

	private final DimensionArrangement arrangement;

	private int sizeC;

	@SuppressWarnings( "rawtypes" )
	private final IterableIntervalProjector2D[] projectors;

	/*
	 * CONSTRUCTOR
	 */

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public VirtualSequence( final RandomAccessibleInterval< ? > source, final DimensionArrangement arrangement )
	{
		super( source.toString() + " - " + arrangement );
		this.arrangement = arrangement;

		if (source.numDimensions() != arrangement.numDimensions()) {
			throw new IllegalArgumentException( "Source does not have the same dimensionality that of the declared dimension arrangment. Expected "
					+ arrangement.numDimensions() + " but got " + source.numDimensions() + "." );
		}

		sizeX = ( int ) source.dimension( 0 );
		sizeY = ( int ) source.dimension( 1 );

		if ( arrangement.hasZ )
		{
			minZ = ( int ) source.min( arrangement.dimZ );
			maxZ = ( int ) source.max( arrangement.dimZ );
			sizeZ = ( int ) source.dimension( arrangement.dimZ );
		}
		else
		{
			minZ = 0;
			maxZ = 0;
			sizeZ = 1;
		}

		if ( arrangement.hasT )
		{
			sizeT = ( int ) source.dimension( arrangement.dimT );
		}
		else
		{
			sizeT = 1;
		}

		if ( arrangement.hasC )
		{
			sizeC = ( int ) source.dimension( arrangement.dimC );
		}
		else
		{
			sizeC = 1;
		}

		final Type rawType = ( Type ) Util.getTypeFromInterval( source );
		if ( !( rawType instanceof NativeType ) ) { throw new IllegalArgumentException( "Non-native types are unsupported, got : " + rawType ); }

		final NativeType rt = ( NativeType ) rawType;
		final NativeType type;
		final DataType dataType;
		final Converter< ?, NativeType > converter;
		final boolean signed;

		if ( rt instanceof UnsignedByteType )
		{
			type = new UnsignedByteType();
			converter = new RealUnsignedByteConverter( 0, 255 );
			signed = false;
			dataType = DataType.UBYTE;
		}
		else if ( rt instanceof UnsignedShortType )
		{
			type = new UnsignedShortType();
			converter = new RealUnsignedShortConverter( 0, 65535 );
			signed = false;
			dataType = DataType.USHORT;
		}
		else if ( rt instanceof FloatType )
		{
			type = new FloatType();
			converter = new RealFloatConverter();
			signed = false;
			dataType = DataType.FLOAT;
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported data type: " + rt );
		}

		final RandomAccessibleInterval< ? > rai = Views.isZeroMin( source ) ? source : Views.zeroMin( source );

		this.projectors = new IterableIntervalProjector2D[ sizeC ];

		if ( arrangement.hasC )
		{
			/*
			 * Multi C image -> We make a composite out of it. The resulting
			 * IcyBufferedImage will NOT be virtual: it will have all channels
			 * in memory at once, and all channels will be mapped. I feel like
			 * it is a reasonable choice (sizeC is typically small compared to
			 * sizeT or sizeZ).
			 */
			image = new IcyBufferedImage( sizeX, sizeY, sizeC, dataType );
			for ( int c = 0; c < sizeC; c++ )
			{
				final long[] targetDims = new long[] { sizeX, sizeY };
				final ArrayImg img;
				switch ( dataType )
				{
				/*
				 * We need to expose the actual primitive array, with the right
				 * class, so we have to treat case by case.
				 */
				case FLOAT:
					img = ArrayImgs.floats( image.getDataXYAsFloat( c ), targetDims );
					break;
				case UBYTE:
					img = ArrayImgs.unsignedBytes( image.getDataXYAsByte( c ), targetDims );
					break;
				case USHORT:
					img = ArrayImgs.unsignedShorts( image.getDataXYAsShort( c ), targetDims );
					break;
				default:
					throw new IllegalArgumentException( "Unsupported data type: " + dataType );
				}

				final RandomAccessibleInterval slice = Views.hyperSlice( rai, arrangement.dimC, c );
				final IterableIntervalProjector2D projector = new IterableIntervalProjector2D( 0, 1, slice, img, converter );
				projector.map();
				projectors[ c ] = projector;
			}
		}
		else
		{
			final ArrayImg img = new ArrayImgFactory().create( new long[] { sizeX, sizeY }, type );
			final IterableIntervalProjector2D projector = new IterableIntervalProjector2D( 0, 1, rai, img, converter );
			projector.map();
			projectors[ 0 ] = projector;

			final Object data = ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray();
			image = new IcyBufferedImage( sizeX, sizeY, data, signed );
		}
		onImageAdded( image );


		volumetricImage = new VirtualVolumetricImage();
	}

	/*
	 * METHODS
	 */

	@Override
	public IcyBufferedImage getImage( final int t, final int z )
	{
		if ( previousT != t || previousZ != z )
		{
			for ( @SuppressWarnings( "rawtypes" )
			final IterableIntervalProjector2D projector : projectors )
			{
				projector.setPosition( z, arrangement.targetDimZ );
				projector.setPosition( t, arrangement.targetDimT );
				projector.map();
			}
			previousT = t;
			previousZ = z;
		}
		return image;
	}

	@Override
	public VolumetricImage getVolumetricImage( final int t )
	{
		if ( t != previousT )
		{
			for ( @SuppressWarnings( "rawtypes" )
			final IterableIntervalProjector2D projector : projectors )
			{
				projector.setPosition( t, arrangement.targetDimT );
				projector.map();
			}
			previousT = t;
		}
		return volumetricImage;
	}

	@Override
	public int getSizeT()
	{
		return sizeT;
	}

	@Override
	public int getSizeZ()
	{
		return sizeZ;
	}

	@Override
	public int getSizeZ( final int t )
	{
		return sizeZ;
	}

	@Override
	public int getSizeX()
	{
		return sizeX;
	}

	@Override
	public int getSizeY()
	{
		return sizeY;
	}

	@Override
	public ArrayList< IcyBufferedImage > getAllImage()
	{
		throw new UnsupportedOperationException( "VirtualSequence cannot return a collection of its content." );
	}

	@Override
	public ArrayList< VolumetricImage > getAllVolumetricImage()
	{
		throw new UnsupportedOperationException( "VirtualSequence cannot return a collection of its content." );
	}

	public IcyBufferedImage getRef()
	{
		return image;
	}

	/*
	 * VIRTUAL VOLUMETRIC IMAGE.
	 */

	private class VirtualVolumetricImage extends VolumetricImage
	{
		@Override
		public ArrayList< IcyBufferedImage > getAllImage()
		{
			throw new UnsupportedOperationException( "VirtualVolumetricImage cannot return a collection of its content." );
		}

		@Override
		public TreeMap< Integer, IcyBufferedImage > getImages()
		{
			throw new UnsupportedOperationException( "VirtualVolumetricImage cannot return a collection of its content." );
		}

		@Override
		public IcyBufferedImage getImage( final int z )
		{
			if ( previousZ != z )
			{
				for ( @SuppressWarnings( "rawtypes" )
				final IterableIntervalProjector2D projector : projectors )
				{
					projector.setPosition( z, arrangement.targetDimZ );
					projector.map();
				}
				previousZ = z;
			}
			return image;
		}

		@Override
		public IcyBufferedImage getFirstImage()
		{
			return getImage( minZ );
		}

		@Override
		public IcyBufferedImage getLastImage()
		{
			return getImage( maxZ );
		}

		@Override
		public int getNumImage()
		{
			return sizeZ;
		}

		@Override
		public int getSize()
		{
			return sizeZ;
		}

		@Override
		public void clear()
		{
			throw new UnsupportedOperationException( "VirtualVolumetricImage cannot clear its content." );
		}

		@Override
		public void setImage( final int z, final IcyBufferedImage image )
		{
			throw new UnsupportedOperationException( "VirtualVolumetricImage cannot set its content." );
		}

		@Override
		public boolean isEmpty()
		{
			return sizeZ < 1;
		}

		@Override
		public boolean removeImage( final int z )
		{
			throw new UnsupportedOperationException( "VirtualVolumetricImage cannot remove an image from its content." );
		}

		@Override
		public void pack()
		{}
	}

}
