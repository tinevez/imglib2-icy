package plugins.tinevez.imglib2icy;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import org.bioimageanalysis.icy.extension.plugin.abstract_.PluginActionable;
import org.bioimageanalysis.icy.gui.dialog.MessageDialog;
import org.bioimageanalysis.icy.model.sequence.Sequence;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2Test< T extends RealType< T > > extends PluginActionable
{

	public static final String LIB_NAME = "ImgLib2-Icy";

	public static final String LIB_VERSION = "v7.1.5";

	@Override
	public void run()
	{
		final Sequence sequence = getActiveSequence();
		if ( null == sequence )
		{
			MessageDialog.showDialog( LIB_NAME + " " + LIB_VERSION,
					"Please select an image fist.",
					MessageDialog.INFORMATION_MESSAGE );
			return;
		}

		final Img< T > img = ImgLib2IcyFunctions.wrap( sequence );
		final DimensionArrangement dimArrangement = ImgLib2IcyFunctions.getDimensionArrangement( sequence );

		MessageDialog.showDialog( LIB_NAME + " " + LIB_VERSION,
				"<html>"
						+ "<h2>ImgLib2 wrapper is working properly.</h2> "
						+ "Wrapping resulted in an ImgLib2 data structure"
						+ "<ul>"
						+ "<li>of size " + Util.printInterval( img ) + "</li>"
						+ "<li>of dimensions " + dimArrangement + "</li>"
						+ "<li>of type " + img.firstElement().getClass() + "</li>"
						+ "</ul>"
						+ "</html>",
				MessageDialog.PLAIN_MESSAGE );
	}

}
