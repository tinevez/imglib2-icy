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

package plugins.tinevez.imglib2icy;

import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import fr.icy.gui.dialog.MessageDialog;
import fr.icy.model.sequence.Sequence;
import plugins.adufour.ezplug.EzPlug;
import plugins.adufour.ezplug.EzVarBoolean;
import plugins.tinevez.imglib2icy.VirtualSequence.DimensionArrangement;

import javax.swing.*;

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
			MessageDialog.showDialog( "Please open a sequence first.", JOptionPane.ERROR_MESSAGE );
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
