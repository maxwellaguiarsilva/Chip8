package com.maxwell.chip8;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

@SuppressWarnings( { "serial" } )
public class MainFrame extends Frame
{
  
  private Container container;
  
  public MainFrame( String title )
  {
    
    super( title );
    
    // this.setUndecorated( true );
    this.container = new Panel( null );
    this.setLayout( new BorderLayout( ) );
    this.add( this.container, BorderLayout.CENTER );
    new AwtGUI( this.container );
    MainFrame.this.pack( );
    this.setLocationRelativeTo( null );
    this.setVisible( true );
    this.addWindowListener( new LocalWindowAdapter( ) );
    this.addComponentListener( new LocalComponentAdapter( ) );
    
  }
  
  private class LocalWindowAdapter extends WindowAdapter
  {
    
    @Override
    public void windowClosing( WindowEvent we )
    {
      
      System.exit( 0 );
      
    }
    
  }
  
  private class LocalComponentAdapter extends ComponentAdapter
  {
    @Override
    public void componentResized( ComponentEvent e )
    {
      MainFrame.this.pack( );
    }
  }
  
}
