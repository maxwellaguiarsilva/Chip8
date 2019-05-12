package com.maxwell.chip8;

public class Display
{
  
  public static final int NUM_COLS = 0x40;              // 64 Cols
  public static final int NUM_ROWS = 0x20;              // 32 Rows
                                                         
  private byte [ ]        display  = new byte [ 0x100 ]; // 2048 bits
                                                         
  private UserInterface   userInterface;
  
  public Display( UserInterface userInterface )
  {
    this.userInterface = userInterface;
    this.clear( );
    this.render( );
    
  }
  
  public boolean drawSprite( int left, int top, byte [ ] data )
  {
    
    boolean flag = false;
    int count = 0, index1, index2, rol, filter1, filter2;
    
    for( count = 0; count < data.length; count++ )
    {
      index1 = ( int ) Math.floor( ( left + ( top + count ) * NUM_COLS ) / 8 )
          % this.display.length;
      index2 = ( index1 + 1 ) % this.display.length;
      
      rol = left % 8;
      filter1 = ( data [ count ] & Chip8.MASK_8BITS ) >> rol;
      filter2 = ( data [ count ] & Chip8.MASK_8BITS ) << 8 - rol & 0xFF;
      
      // if( ( index1 % NUM_COLS ) > ( index2 % NUM_COLS ) )
      // index2 -= NUM_COLS;
      
      this.display [ index1 ] ^= filter1;
      this.display [ index2 ] ^= filter2;
      if( ( ( this.display [ index1 ] | this.display [ index2 ] << 8 ) & ( filter1 | filter2 << 8 ) ) != ( filter1 | filter2 << 8 ) )
        flag = true;
    }
    
    return flag;
    
  }
  
  public void clear( )
  {
    
    int count = 0;
    for( count = 0; count < this.display.length; count++ )
      this.display [ count ] = 0;
    
  }
  
  public void render( )
  {
    this.userInterface.updateDisplay( this.display );
  }
  
  public void beep( )
  {
    // TODO Auto-generated method stub
    this.userInterface.beep( );
  }
  
}