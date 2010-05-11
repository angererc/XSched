/* This file was generated by SableCC (http://www.sablecc.org/). */

package soot.jimple.parser.node;

import soot.jimple.parser.analysis.*;

public final class TAnnotation extends Token
{
    public TAnnotation()
    {
        super.setText("annotation");
    }

    public TAnnotation(int line, int pos)
    {
        super.setText("annotation");
        setLine(line);
        setPos(pos);
    }

    public Object clone()
    {
      return new TAnnotation(getLine(), getPos());
    }

    public void apply(Switch sw)
    {
        ((Analysis) sw).caseTAnnotation(this);
    }

    public void setText(String text)
    {
        throw new RuntimeException("Cannot change TAnnotation text.");
    }
}
