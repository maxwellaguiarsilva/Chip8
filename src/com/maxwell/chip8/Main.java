package com.maxwell.chip8;

import java.applet.Applet;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

@SuppressWarnings( "serial" )
public class Main extends Applet
{
  
  @Override
  public void init( )
  {
    if( this.getParameter( "width" ) != null
        && this.getParameter( "height" ) != null )
      new AwtGUI( this, new Integer( this.getParameter( "width" ) ),
          new Integer( this.getParameter( "height" ) ) );
    this.requestFocus( );
    this.requestFocusInWindow( );
  }
  
  private static class InvokeRunnable implements Runnable
  {
    
    public InvokeRunnable( )
    {
      
    }
    
    @Override
    public void run( )
    {
      // TODO Auto-generated method stub
      new MainFrame( "Chip8" );
    }
    
  }
  
  public static void main( String [ ] args ) throws IOException
  {
    
    // TODO Auto-generated method stub
    try
    {
      java.awt.EventQueue.invokeAndWait( new InvokeRunnable( ) );
    } catch( InvocationTargetException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace( );
    } catch( InterruptedException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace( );
    }
    
  }
  
}