package plugins.tinevez.imglib2icy;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2SequenceSplitter< T extends NumericType< T > & RealType< T > > extends EzPlug
{

	private final EzVarBoolean splitCUI = new EzVarBoolean( "Split C", true );

	private final EzVarBoolean splitZUI = new EzVarBoolean( "Split Z", false );

	private final EzVarBoolean splitTUI = new EzVarBoolean( "Split T", false );

	@Override
	protected void initialize()
	{
		addEzComponent( splitCUI );
		addEzComponent( splitZUI );
		addEzComponent( splitTUI );
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
		
		final boolean splitC = splitCUI.getValue( true );
		final boolean splitZ = splitZUI.getValue( true );
		final boolean splitT = splitTUI.getValue( true );

		final List< Img< T > > imgs = ImgLib2IcySplitSequenceAdapter.wrap( sequence, splitC, splitZ, splitT );
		final DimensionArrangement dim = ImgLib2IcySplitSequenceAdapter.getDimensionArrangement( sequence, splitC, splitZ, splitT );

		for ( final Img< T > img : imgs )
		{
			final Sequence seq = ImgLib2IcyFunctions.wrap( img, dim );
			addSequence( seq );
		}
	}

	@Override
	public void clean()
	{}
}
