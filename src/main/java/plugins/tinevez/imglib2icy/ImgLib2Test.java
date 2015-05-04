package plugins.tinevez.imglib2icy;

import icy.gui.dialog.MessageDialog;
import icy.plugin.abstract_.PluginActionable;
import icy.plugin.interface_.PluginLibrary;
import icy.sequence.Sequence;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

public class ImgLib2Test< T extends RealType< T >> extends PluginActionable implements PluginLibrary
{

	public static final String LIB_NAME = "ImgLib2-Icy";

	public static final String LIB_VERSION = "v0.0.3";

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
						+ "<li>of type " + img.firstElement().getClass() + ""
						+ "</ul>"
						+ "</html>",
						MessageDialog.PLAIN_MESSAGE );
	}

}
