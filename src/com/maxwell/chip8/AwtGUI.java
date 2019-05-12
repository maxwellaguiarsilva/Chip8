package com.maxwell.chip8;

import java.awt.AWTEvent;
import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.maxwell.util.Dispatcher;

public class AwtGUI implements UserInterface, Chip8Listener, ActionListener,
    KeyListener, AWTEventListener, ItemListener, MouseListener
{
  
  private static final int                    COMPONENT_WIDTH  = 0x0A,
      COMPONENT_HEIGHT = 0x04, BUTTON_SIZE = 0x07;
  private int                                 DEFAULT_CELLSIZE = 0x07;
  
  private Container                           container;
  
  private DisplayPanel                        displayPanel;
  private Panel                               keysPanel;
  private Button                              bntSwitch, bntReset;
  private Label                               lblRoms;
  private Choice                              choiceRoms;
  private Checkbox                            chxTurbo;
  private LinkedList< Button >                keysButtonSet;
  
  private LinearLayout                        chip8Layout, keysLayout;
  private boolean                             started;
  private Chip8                               chip8;
  
  private Dispatcher< UserInterfaceListener > dispatcher       = new Dispatcher< UserInterfaceListener >( );
  
  private static Color                        LIGHT_COLOR      = new Color(
                                                                   0xDDDDDD ),
      DARK_COLOR = new Color( 0x333333 );
  
  private static final String                 STR_START        = "Start",
      STR_STOP = "Stop", STR_RESET = "Reset", STR_ROMS = "ROMS: ",
      STR_TURBO = "Turbo", keyNameSet = "1234QWERASDFZXCV",
      DEFAULT_ROM = "INVADERS";
  private static final Integer [ ]            keyCodeSet       = {
      KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3, KeyEvent.VK_4,
      KeyEvent.VK_Q, KeyEvent.VK_W, KeyEvent.VK_E, KeyEvent.VK_R,
      KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, KeyEvent.VK_F,
      KeyEvent.VK_Z, KeyEvent.VK_X, KeyEvent.VK_C, KeyEvent.VK_V };
  
  public AwtGUI( Container container, int width, int height )
  {
    this.container = container;
    this.bntSwitch = new Button( AwtGUI.STR_START );
    this.bntReset = new Button( AwtGUI.STR_RESET );
    
    this.lblRoms = new Label( AwtGUI.STR_ROMS );
    this.chxTurbo = new Checkbox( AwtGUI.STR_TURBO );
    this.choiceRoms = new Choice( );
    this.keysButtonSet = new LinkedList< Button >( );
    this.keysPanel = new Panel( );
    this.displayPanel = new DisplayPanel( );
    
    for( String romName : this.getAllRoms( ) )
      this.choiceRoms.addItem( romName );
    
    int count = 0;
    
    for( count = 0; count < Chip8.NUM_KEYS; count++ )
      this.keysButtonSet.add( new Button( keyNameSet.substring( count,
          count + 1 ) ) );
    for( Button button : this.keysButtonSet )
      button.addMouseListener( this );
    
    for( Button button : this.keysButtonSet )
      button.setBackground( AwtGUI.LIGHT_COLOR );
    
    this.container.addKeyListener( this );
    
    this.createLayout( container );
    if( width > 0 )
    {
      Dimension total = this.chip8Layout.getSize( );
      int default1, default2;
      default1 = width / ( total.width / this.DEFAULT_CELLSIZE );
      default2 = height / ( total.height / this.DEFAULT_CELLSIZE );
      this.DEFAULT_CELLSIZE = default1 < default2? default1 : default2;
      this.createLayout( container );
    }
    
    Toolkit.getDefaultToolkit( ).addAWTEventListener( this,
        AWTEvent.KEY_EVENT_MASK );
    
    this.chip8 = new Chip8( this );
    this.chip8.addListener( this );
    
    this.choiceRoms.select( AwtGUI.DEFAULT_ROM );
    try
    {
      this.loadROM( );
    } catch( RuntimeException e )
    {
    }
    
  }
  
  public AwtGUI( Container container )
  {
    this( container, 0, 0 );
  }
  
  private void createLayout( Container container )
  {
    container.removeAll( );
    container.setFont( new Font( null, 0, this.DEFAULT_CELLSIZE
        * ( AwtGUI.COMPONENT_HEIGHT - 2 ) ) );
    
    this.chip8Layout = new LinearLayout( container, Display.NUM_ROWS,
        this.DEFAULT_CELLSIZE );
    
    this.keysLayout = new LinearLayout( this.keysPanel, AwtGUI.BUTTON_SIZE,
        this.DEFAULT_CELLSIZE );
    this.keysLayout.useBorder( false );
    
    this.displayPanel.setCellSize( this.DEFAULT_CELLSIZE );
    this.chip8Layout.add( this.displayPanel, Display.NUM_COLS );
    this.chip8Layout.add( this.keysPanel, Display.NUM_ROWS );
    this.chip8Layout.addLine( AwtGUI.COMPONENT_HEIGHT );
    this.chip8Layout.add( this.bntSwitch, AwtGUI.COMPONENT_WIDTH );
    this.chip8Layout.add( this.bntReset, AwtGUI.COMPONENT_WIDTH );
    this.chip8Layout.add( this.chxTurbo, AwtGUI.COMPONENT_WIDTH );
    this.chip8Layout.add( this.lblRoms, AwtGUI.COMPONENT_WIDTH
        - AwtGUI.COMPONENT_HEIGHT / 2 );
    this.chip8Layout.add( this.choiceRoms, AwtGUI.COMPONENT_WIDTH
        + AwtGUI.COMPONENT_WIDTH + AwtGUI.COMPONENT_HEIGHT / 2 );
    
    this.keysPanel.removeAll( );
    for( Button button : this.keysButtonSet )
    {
      this.keysLayout.add( button, AwtGUI.BUTTON_SIZE );
      if( this.keysButtonSet.indexOf( button ) % 4 + 1 == 4 )
        this.keysLayout.addLine( AwtGUI.BUTTON_SIZE );
    }
    this.keysLayout.removeLine( );
    
    for( Component component : this.container.getComponents( ) )
      if( component instanceof Button )
        ( ( Button ) component ).addActionListener( this );
      else
        if( component.equals( this.choiceRoms ) )
          ( ( Choice ) component ).addItemListener( this );
    
    this.chxTurbo.addItemListener( this );
    
    container.repaint( );
  }
  
  public Dimension getSize( )
  {
    return this.chip8Layout.getSize( );
  }
  
  @Override
  public void updateDisplay( byte [ ] display )
  {
    this.displayPanel.updateDisplay( display );
  }
  
  @Override
  public void beep( )
  {
    
  }
  
  private List< String > getAllRoms( )
  {
    
    List< String > list = new ArrayList< String >( );
    
    try
    {
      ZipInputStream zin = new ZipInputStream( this.getClass( )
          .getResourceAsStream( "/ROMS/roms.zip" ) );
      
      ZipEntry entry;
      while( ( entry = zin.getNextEntry( ) ) != null )
        list.add( entry.getName( ) );
      zin.close( );
    } catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace( );
    } catch( NullPointerException e )
    {
      
    }
    
    return list;
    
  }
  
  private byte [ ] getROMData( String name )
  {
    try
    {
      ZipInputStream zin = new ZipInputStream( this.getClass( )
          .getResourceAsStream( "/ROMS/roms.zip" ) );
      
      ZipEntry entry;
      while( ( entry = zin.getNextEntry( ) ) != null )
        if( entry.getName( ).equals( name ) )
        {
          byte [ ] result = new byte [ ( int ) entry.getSize( ) ];
          for( int count = 0; count < entry.getSize( ); count++ )
            result [ count ] = ( byte ) zin.read( );
          zin.close( );
          return result;
        }
      zin.close( );
    } catch( IOException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace( );
    } catch( NullPointerException e )
    {
      e.printStackTrace( );
    }
    throw new RuntimeException( "ROM not founded!" );
    
  }
  
  @SuppressWarnings( "serial" )
  private class DisplayPanel extends Canvas
  {
    private int      width, height, cellSize;
    private Color    backColor, frontColor;
    private byte [ ] display = new byte [ 0x100 ];
    
    public DisplayPanel( )
    {
      
      this.setFrontColor( AwtGUI.DARK_COLOR );
      this.setBackColor( AwtGUI.LIGHT_COLOR );
      // this.setBackground( this.backColor );
      this.setCellSize( AwtGUI.this.DEFAULT_CELLSIZE );
      
      this.setIgnoreRepaint( true );
    }
    
    private void render( )
    {
      int count = 0, bitCount = 0;
      int location, left, top;
      
      BufferStrategy strategy = this.getBufferStrategy( );
      Graphics graphics = strategy.getDrawGraphics( );
      
      for( byte byteDisplay : this.display )
      {
        for( bitCount = 0; bitCount < Chip8.NUM_BITS; bitCount++ )
        {
          location = count * 8 + bitCount;
          left = location % Display.NUM_COLS;
          top = ( int ) Math.floor( location / Display.NUM_COLS );
          graphics
              .setColor( ( byteDisplay >> 7 - bitCount & 1 ) == 0? this.backColor
                  : this.frontColor );
          graphics.fillRect( left * this.cellSize, top * this.cellSize,
              this.cellSize, this.cellSize );
        }
        count++ ;
      }
      strategy.show( );
      // Toolkit.getDefaultToolkit().sync();
      
    }
    
    public void updateDisplay( byte [ ] display )
    {
      
      this.display = display.clone( );
      if( this.getBufferStrategy( ) == null )
        try
        {
          this.createBufferStrategy( 2 );
        } catch( IllegalStateException e )
        {
          return;
        }
      this.render( );
      
    }
    
    public void setCellSize( int cellSize )
    {
      
      this.cellSize = cellSize;
      this.width = cellSize * Display.NUM_COLS;
      this.height = cellSize * Display.NUM_ROWS;
      this.setPreferredSize( new Dimension( this.width, this.height ) );
      
    }
    
    public void setBackColor( Color color )
    {
      this.backColor = color;
    }
    
    public void setFrontColor( Color color )
    {
      this.frontColor = color;
    }
    
  }
  
  @Override
  public void onStatusChange( boolean started )
  {
    // TODO Auto-generated method stub
    this.bntSwitch.setLabel( started? AwtGUI.STR_STOP : AwtGUI.STR_START );
    this.started = started;
    
  }
  
  @Override
  public void actionPerformed( ActionEvent event )
  {
    // TODO Auto-generated method stub
    if( event.getSource( ).equals( this.bntSwitch ) )
      if( this.started )
        this.chip8.stop( );
      else
        this.chip8.start( );
    
    if( event.getSource( ).equals( this.bntReset ) )
      this.loadROM( );
    
  }
  
  @Override
  public void keyPressed( KeyEvent event )
  {
    // TODO Auto-generated method stub
    byte keyNumber = ( byte ) Arrays.asList( AwtGUI.keyCodeSet ).indexOf(
        event.getKeyCode( ) );
    if( keyNumber != -1 )
    {
      this.dispatcher.dispatch( "onKeyDown", new Object [ ] { keyNumber } );
      this.keysButtonSet.get( keyNumber ).setBackground( AwtGUI.DARK_COLOR );
    }
    
  }
  
  @Override
  public void keyReleased( KeyEvent event )
  {
    
    byte keyNumber = ( byte ) Arrays.asList( AwtGUI.keyCodeSet ).indexOf(
        event.getKeyCode( ) );
    if( keyNumber != -1 )
    {
      this.dispatcher.dispatch( "onKeyUp", new Object [ ] { keyNumber } );
      this.keysButtonSet.get( keyNumber ).setBackground( AwtGUI.LIGHT_COLOR );
    }
    
  }
  
  @Override
  public void keyTyped( KeyEvent event )
  {
    // TODO Auto-generated method stub
    
  }
  
  private AWTEvent lastEvent;
  
  @Override
  public void eventDispatched( AWTEvent event )
  {
    
    if( this.lastEvent == event )
      return;
    this.lastEvent = event;
    // TODO Auto-generated method stub
    if( event instanceof KeyEvent )
      this.container.dispatchEvent( event );
  }
  
  @Override
  public void itemStateChanged( ItemEvent event )
  {
    // TODO Auto-generated method stub
    if( event.getSource( ).equals( this.choiceRoms ) )
      this.loadROM( );
    if( event.getSource( ).equals( this.chxTurbo ) )
      this.chip8.setTurbo( event.getStateChange( ) == 1 );
    
  }
  
  private void loadROM( )
  {
    byte [ ] romData = this.getROMData( this.choiceRoms.getSelectedItem( ) );
    this.dispatcher.dispatch( "onLoadROM", new Object [ ] { romData } );
  }
  
  @Override
  public void addListener( UserInterfaceListener listener )
  {
    // TODO Auto-generated method stub
    this.dispatcher.addListener( listener );
  }
  
  @Override
  public void removeListener( UserInterfaceListener listener )
  {
    // TODO Auto-generated method stub
    this.dispatcher.removeListener( listener );
  }
  
  @Override
  public void mousePressed( MouseEvent mouseEvent )
  {
    byte keyNumber = ( byte ) this.keysButtonSet.indexOf( mouseEvent
        .getSource( ) );
    if( keyNumber != -1 )
      this.dispatcher.dispatch( "onKeyDown", new Object [ ] { keyNumber } );
  }
  
  @Override
  public void mouseReleased( MouseEvent mouseEvent )
  {
    byte keyNumber = ( byte ) this.keysButtonSet.indexOf( mouseEvent
        .getSource( ) );
    if( keyNumber != -1 )
      this.dispatcher.dispatch( "onKeyUp", new Object [ ] { keyNumber } );
  }
  
  @Override
  public void mouseEntered( MouseEvent mouseEvent )
  {
  }
  
  @Override
  public void mouseExited( MouseEvent mouseEvent )
  {
  }
  
  @Override
  public void mouseClicked( MouseEvent mouseEvent )
  {
  }
  
}
