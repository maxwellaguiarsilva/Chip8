/*
 * Continue frameloop
 */

package com.maxwell.chip8;

import java.util.Calendar;

import com.maxwell.chip8.CPU.KeyCallBack;
import com.maxwell.util.Dispatcher;
import com.maxwell.util.Listenable;

public class Chip8 implements Listenable< Chip8Listener >
{
  
  public static final int             MASK_8BITS  = 0xFF, NUM_BITS = 0x08,
      NUM_KEYS = 0x10;
  
  private Display                     display;
  private boolean                     drawFlag    = false, first = true,
      running = false, stopped = true, turbo = false, newROM = false;
  
  private KeyCallBack                 keyCallBack = null;
  
  private boolean [ ]                 keys        = new boolean [ Chip8.NUM_KEYS ];
  private byte [ ]                    romData     = null;
  
  private long                        lastTime    = 0;
  private static final int            HERTZ       = 60, STEPS = 0x10,
      FRAME_TIME = ( int ) Math.floor( 1000 / Chip8.HERTZ ), TURBO_VALUE = FRAME_TIME / 5;
  private CPU                         cpu;
  
  private Dispatcher< Chip8Listener > dispatcher  = new Dispatcher< Chip8Listener >( );
  private LocalListener               localListener;                                   // Listener
                                                                                        // Chip8
                                                                                        // and
                                                                                        // UserInterface
                                                                                        
  public Chip8( UserInterface userInterface )
  {
    
    int count = 0;
    
    this.display = new Display( userInterface );
    while( count < 0x10 )
      this.keys [ count++ ] = false;
    
    this.localListener = new LocalListener( );
    userInterface.addListener( this.localListener );
    
    this.cpu = new CPU( this );
    
    this.addListener( this.localListener );
    
  }
  
  public void reset( )
  {
    this.keyCallBack = null;
  }
  
  private void runFrameLoop( )
  {
    new Thread( this.frameLoopRunnable ).start( );
  }
  
  public void start( )
  {
    if( !this.stopped )
      return;
    this.running = true;
    this.stopped = false;
    this.first = true;
    this.drawFlag = true; 
    
    this.runFrameLoop( );
    
  }
  
  public void stop( )
  {
    // TODO Auto-generated method stub
    this.running = false;
    if( this.keyCallBack != null ) 
      this.runFrameLoop( ); 
  }
  
  private class FrameLoopRunnable implements Runnable
  {
    
    @Override
    public void run( )
    {
      // TODO Auto-generated method stub
      
      FRAMELOOP: while( true )
      {
        if( Chip8.this.first )
        {
          Chip8.this.first = false;
          Chip8.this.dispatcher.dispatch( "onStatusChange",
              new Object [ ] { true } );
        }
        
        int count = 0;
        
        for( count = 0; count < Chip8.STEPS; count++ )
          if( Chip8.this.running && Chip8.this.keyCallBack == null )
            Chip8.this.cpu.emulate( );
          else
            break;
        
        if( Chip8.this.drawFlag )
        {
          Chip8.this.display.render( );
          Chip8.this.drawFlag = false;
        }
        
        long now = Calendar.getInstance( ).getTimeInMillis( );
        if( now > Chip8.this.lastTime + Chip8.FRAME_TIME )
        {
          Chip8.this.cpu.handleTimers( );
          Chip8.this.lastTime = now;
        }
        
        if( Chip8.this.running && Chip8.this.keyCallBack == null )
          try
          {
            Thread.sleep( Chip8.this.turbo? Chip8.FRAME_TIME/Chip8.TURBO_VALUE : Chip8.FRAME_TIME );
          } catch( InterruptedException e )
          {
            
          }
        else
        {
          // onStop 
          if( !Chip8.this.running )
            Chip8.this.dispatcher.dispatch( "onStatusChange", new Object [ ] { false } );
          break FRAMELOOP;
        }
        
      }
      
    }
  }
  
  private Runnable frameLoopRunnable = new FrameLoopRunnable( );
  
  public Display getDisplay( )
  {
    // TODO Auto-generated method stub
    return this.display;
  }
  
  public boolean keyPressed( int keyNumber )
  {
    // TODO Auto-generated method stub
    return this.keys [ keyNumber ];
  }
  
  public void setDrawFlag( )
  {
    // TODO Auto-generated method stub
    this.drawFlag = true;
  }
  
  public void setTurbo( boolean turbo )
  {
    Chip8.this.turbo = turbo;
  }
  
  public void waitKey( KeyCallBack keyCallBack )
  {
    // TODO Auto-generated method stub
    this.keyCallBack = keyCallBack;
  }
  
  @Override
  public void addListener( Chip8Listener listener )
  {
    this.dispatcher.addListener( listener );
  }
  
  @Override
  public void removeListener( Chip8Listener listener )
  {
    this.dispatcher.removeListener( listener );
  }
  
  private void newROM( )
  {
    
    this.newROM = false;
    this.cpu.loadROM( this.romData );
    this.reset( );
    this.start( );
    
  }
  
  private class LocalListener implements Chip8Listener, UserInterfaceListener
  {
    
    @Override
    public void onStatusChange( boolean started )
    {
      // Load the new ROM only if it is stopped
      if( started ) 
        return;
      
      Chip8.this.stopped = true;
      if( Chip8.this.newROM )
        Chip8.this.newROM( );
      
    }
    
    @Override
    public void onKeyDown( byte keyNumber )
    {
      // TODO Auto-generated method stub
      Chip8.this.keys [ keyNumber ] = true;
      if( Chip8.this.keyCallBack != null && Chip8.this.running )
      {
        Chip8.this.keyCallBack.callBack( keyNumber );
        Chip8.this.keyCallBack = null;
        Chip8.this.runFrameLoop( );
      }
    }
    
    @Override
    public void onKeyUp( byte keyNumber )
    {
      // TODO Auto-generated method stub
      Chip8.this.keys [ keyNumber ] = false;
    }
    
    @Override
    public void onLoadROM( byte [ ] romData )
    {
      // TODO Auto-generated method stub
      Chip8.this.newROM = true;
      Chip8.this.romData = romData;
      if( Chip8.this.stopped )
        Chip8.this.newROM( );
      else
        Chip8.this.stop( );
    }
    
  }
  
}
