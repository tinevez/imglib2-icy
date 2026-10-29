/*
 * Copyright (c) 2010-2026. Institut Pasteur.
 *
 * This file is part of Icy.
 * Icy is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Icy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Icy. If not, see <https://www.gnu.org/licenses/>.
 */

package icy.plugins.imglib2;

import fr.icy.extension.plugin.annotation_.IcyPluginCategory;
import fr.icy.extension.plugin.annotation_.IcyPluginName;
import fr.icy.extension.plugin.annotation_.IcyPluginTags;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.RealType;
import net.imglib2.util.Util;
import fr.icy.extension.plugin.abstract_.PluginActionable;
import fr.icy.gui.dialog.MessageDialog;
import fr.icy.model.sequence.Sequence;
import plugins.tinevez.imglib2icy.ImgLib2IcyFunctions;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

import javax.swing.*;

@IcyPluginName( "ImgLib2 Test")
@IcyPluginCategory("ImgLib2")
@IcyPluginTags({"ImgLib2", "Test"})
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
					JOptionPane.INFORMATION_MESSAGE );
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
				JOptionPane.PLAIN_MESSAGE );
	}

}
