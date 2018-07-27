package net.imglib2.algorithm.math;

import java.util.Map;

import net.imglib2.Localizable;
import net.imglib2.algorithm.math.abstractions.Compare;
import net.imglib2.algorithm.math.abstractions.IBooleanFunction;
import net.imglib2.algorithm.math.abstractions.IFunction;
import net.imglib2.converter.Converter;
import net.imglib2.type.numeric.RealType;

public class IfBoolean extends If
{
	/**
	 * Same as this.a, to avoid casting.
	 */
	final IBooleanFunction abool;
	
	protected IfBoolean( final RealType< ? > scrap, final Compare f1, final IFunction f2, final IFunction f3 )
	{
		super ( scrap, f1, f2, f3 );
		this.abool = f1; // same as this.a
	}

	@Override
	public final void eval( final RealType< ? > output )
	{
		if ( this.abool.evalBoolean( this.scrap ) )
		{
			// Then
			this.b.eval( output );
		} else
		{
			// Else
			this.c.eval( output );
		}
	}

	@Override
	public final void eval( final RealType< ? > output, final Localizable loc )
	{
		if ( this.abool.evalBoolean( output, loc ) )
		{
			// Then
			this.b.eval( output, loc );
		} else
		{
			// Else
			this.c.eval( output, loc );
		}
	}

	@Override
	public IfBoolean reInit( final RealType< ? > tmp, final Map< String, RealType< ? > > bindings, final Converter< RealType< ? >, RealType< ? > > converter )
	{
		return new IfBoolean( tmp.copy(), ( Compare ) this.a.reInit( tmp, bindings, converter ), this.b.reInit( tmp, bindings, converter ), this.c.reInit( tmp, bindings, converter ) );
	}
}