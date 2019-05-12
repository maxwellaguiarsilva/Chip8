package com.maxwell.chip8;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.LinkedList;

public class LinearLayout
{
  
  private static final int               DEFAULT_CELLSIZE = 0x08;
  private LinkedList< LineComponentSet > lineComponentSet = new LinkedList< LineComponentSet >( );
  private HashMap< Component, Integer >  componentSizeSet = new HashMap< Component, Integer >( );
  
  private int                            cellSize, width = 0, height = 0;
  private boolean                        useBorder        = true;
  
  private Container                      container;
  
  public LinearLayout( Container container, int firstLineSize, int cellSize )
  {
    this.setLayoutContainer( container );
    this.addLine( ( firstLineSize < 1 )? 1 : firstLineSize );
    this.setCellSize( cellSize ); // Organize Components
  }
  
  public LinearLayout( Container container, int firstLineSize )
  {
    this( container, firstLineSize, LinearLayout.DEFAULT_CELLSIZE );
  }
  
  public void addLine( int lineHeight )
  {
    LineComponentSet lineComponentSet = new LineComponentSet( lineHeight );
    this.lineComponentSet.add( lineComponentSet );
    this.organizeComponents( );
  }
  
  public void removeLine( )
  {
    LinkedList< Component > componentSet = this.lineComponentSet.removeLast( )
        .getComponentSet( );
    for( Component component : componentSet )
    {
      this.container.remove( component );
      this.componentSizeSet.remove( component );
    }
    this.organizeComponents( );
  }
  
  public void add( Component component, int width )
  {
    this.componentSizeSet.put( component, width );
    LinkedList< Component > componentSet = this.lineComponentSet.getLast( )
        .getComponentSet( );
    componentSet.add( component );
    this.container.add( component );
    this.organizeComponents( );
  }
  
  private void organizeComponents( )
  {
    int cellSize = this.getCellSize( );
    
    int initialTop = ( this.useBorder )? cellSize : 0;
    int initialLeft = ( this.useBorder )? cellSize : 0; 
    
    int componentTop = initialTop; 
    for( LineComponentSet lineComponentSet : this.lineComponentSet )
    {
      int lineHeight = lineComponentSet.getLineHeight( );
      int componentLeft = initialLeft;
      
      for( Component component : lineComponentSet.getComponentSet( ) )
      {
        int componentWidth = this.componentSizeSet.get( component ) * cellSize, componentHeight = lineHeight
            * cellSize;
        component.setBounds( componentLeft, componentTop, componentWidth,
            componentHeight );
        componentLeft += componentWidth + cellSize;
      }
      this.width = ( this.width < componentLeft )? componentLeft : this.width;
      componentTop += lineHeight * cellSize + cellSize;
    }
    
    this.height = componentTop;
    if( !this.useBorder )
      this.height -= cellSize;
    this.width -= cellSize;
    
    this.container.setSize( this.getSize( ) );
    this.container.setPreferredSize( this.getSize( ) );
    
  }
  
  public void useBorder( boolean useBorder )
  {
    this.useBorder = useBorder;
  }
  
  public Dimension getSize( )
  {
    return new Dimension( this.width, this.height );
  }
  
  private void setLayoutContainer( Container container )
  {
    // TODO Auto-generated method stub
    this.container = container;
    this.container.setLayout( null );
  }
  
  public int getCellSize( )
  {
    return this.cellSize;
  }
  
  public void setCellSize( int cellSize )
  {
    this.cellSize = ( cellSize < 1 )? 1 : cellSize;
    this.organizeComponents( );
  }
  
  private class LineComponentSet
  {
    private int                     lineHeight;
    private LinkedList< Component > componentSet = new LinkedList< Component >( );
    
    public int getLineHeight( )
    {
      return this.lineHeight;
    }
    
    public LinkedList< Component > getComponentSet( )
    {
      return this.componentSet;
    }
    
    public LineComponentSet( int lineHeight )
    {
      // TODO Auto-generated constructor stub
      this.lineHeight = lineHeight;
    }
  }
  
}
