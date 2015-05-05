package plugins.tinevez.imglib2icy;

import icy.gui.dialog.MessageDialog;
import icy.sequence.Sequence;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2SequenceSplitter extends EzPlug
{

	private final EzVarBoolean splitC = new EzVarBoolean( "Split C", true );

	private final EzVarBoolean splitZ = new EzVarBoolean( "Split Z", false );

	private final EzVarBoolean splitT = new EzVarBoolean( "Split T", false );

	@Override
	protected void initialize()
	{
		addEzComponent( splitC );
		addEzComponent( splitZ );
		addEzComponent( splitT );
	}
	@Override
	protected void execute()
	{
		final Sequence sequence = getActiveSequence();
		if ( null == sequence )
		{
			MessageDialog.showDialog( "Please open a sequence first.", MessageDialog.ERROR_MESSAGE );
			return;
		}

		final List< Img< UnsignedByteType >> imgs = ImgLib2IcySplitSequenceAdapter.wrapUnsignedByte( sequence,
				splitC.getValue( true ),
				splitZ.getValue( true ),
				splitT.getValue( true ) );

		final DimensionArrangement dim = ImgLib2IcySplitSequenceAdapter.getDimensionArrangement( sequence,
				splitC.getValue( true ),
				splitZ.getValue( true ),
				splitT.getValue( true ) );

		for ( final Img< UnsignedByteType > img : imgs )
		{
			final Sequence seq = ImgLib2IcyFunctions.wrap( img, dim );
			addSequence( seq );
		}
	}


	@Override
	public void clean()
	{}
}
