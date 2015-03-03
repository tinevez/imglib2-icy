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

	/*
	 * CONSTRUCTOR
	 */

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	public VirtualSequence( final RandomAccessibleInterval< ? > source )
	{
		super();

		// Mono channel for now.
		// Right now I suppose I have a XYZT image with each dim of size > 1.

		sizeX = ( int ) source.dimension( 0 );
		sizeY = ( int ) source.dimension( 1 );

		// TODO
		minZ = ( int ) source.min( 2 );
		maxZ = ( int ) source.max( 2 );
		sizeZ = ( int ) source.dimension( 2 );

		// TODO
		minT = ( int ) source.min( 3 );
		maxT = ( int ) source.max( 3 );
		sizeT = ( int ) source.dimension( 3 );

		final Type rawType = ( Type ) Util.getTypeFromInterval( source );
		if ( !( rawType instanceof NativeType ) )
		{
 throw new IllegalArgumentException( "Non-native types are unsupported, got : " + rawType );
		}

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
			throw new IllegalArgumentException( "Unsupported data type: " + type);
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
			projector.setPosition( z, 2 );
			projector.setPosition( t, 3 );
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
			projector.setPosition( t, 3 );
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
				projector.setPosition( z, 2 );
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

