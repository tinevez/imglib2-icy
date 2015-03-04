package net.imglib2.img.display.icy;

import icy.image.IcyBufferedImage;
import icy.sequence.Sequence;
import icy.sequence.VolumetricImage;

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

		XYC( true, false, false, 0, 0, 0 ),
		XYZ( false, true, false, 0, 2, 0 ),
		XYT( false, false, true, 0, 0, 2 ),
		XYCT( true, false, true, 2, 0, 3 ),
		XYZT( false, true, true, 0, 2, 3 ),
		XYCZT( true, true, true, 2, 3, 4 );

		private final boolean hasZ;

		private final int dimZ;

		private final boolean hasT;

		private final int dimT;

		private final boolean hasC;

		private final int dimC;

		private DimensionArrangement( final boolean hasC, final boolean hasZ, final boolean hasT, final int dimC, final int dimZ, final int dimT )
		{
			this.hasC = hasC;
			this.hasZ = hasZ;
			this.hasT = hasT;
			this.dimC = dimC;
			this.dimZ = dimZ;
			this.dimT = dimT;
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
	}

	private final IterableIntervalProjector2D< ?, ? > projector;

	private final IcyBufferedImage image;

	private final VolumetricImage volumetricImage;

	private final int minZ;

	private final int maxZ;

	private final int minT;

	private final int maxT;

	private final int sizeZ;

	private final int sizeT;

	private int previousT = -1;

	private int previousZ = -1;

	private final int sizeX;

	private final int sizeY;

	private final DimensionArrangement arrangement;

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
			sizeZ = 0;
		}

		if ( arrangement.hasT )
		{
			minT = ( int ) source.min( arrangement.dimT );
			maxT = ( int ) source.max( arrangement.dimT );
			sizeT = ( int ) source.dimension( arrangement.dimT );
		}
		else
		{
			minT = 0;
			maxT = 0;
			sizeT = 0;
		}

		final Type rawType = ( Type ) Util.getTypeFromInterval( source );
		if ( !( rawType instanceof NativeType ) ) { throw new IllegalArgumentException( "Non-native types are unsupported, got : " + rawType ); }

		NativeType type = ( NativeType ) rawType;
		final Converter< ?, NativeType > converter;

		if ( type instanceof UnsignedByteType )
		{
			type = new UnsignedByteType();
			converter = new RealUnsignedByteConverter( 0, 255 );
		}
		else if ( type instanceof UnsignedShortType )
		{
			type = new UnsignedShortType();
			converter = new RealUnsignedShortConverter( 0, 65535 );
		}
		else if ( type instanceof FloatType )
		{
			type = new FloatType();
			converter = new RealFloatConverter();
		}
		else
		{
			throw new IllegalArgumentException( "Unsupported data type: " + type );
		}

		final ArrayImg img = new ArrayImgFactory().create( new long[] { sizeX, sizeY }, type );
		this.projector = new IterableIntervalProjector2D( 0, 1, Views.isZeroMin( source ) ? source : Views.zeroMin( source ), img, converter );
		projector.map();

		final Object data = ( ( ArrayDataAccess< ? > ) img.update( null ) ).getCurrentStorageArray();
		image = new IcyBufferedImage( sizeX, sizeY, data, false );
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
			projector.setPosition( z, arrangement.dimZ );
			projector.setPosition( t, arrangement.dimT );
			projector.map();
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
			projector.setPosition( t, arrangement.dimT );
			projector.map();
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
				projector.setPosition( z, arrangement.dimZ );
				projector.map();
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
