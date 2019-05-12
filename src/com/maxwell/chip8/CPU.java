package com.maxwell.chip8;

import java.util.Arrays;

public class CPU
{
  
  private Chip8   chip8;
  private Display display;
  
  byte [ ]        romData    = null, memory = new byte [ 0x1000 ],
      registers = new byte [ 0x10 ];
  int             stackPoint = 0, programCounter = 0, address = 0,
      delayTimer = 0, soundTimer = 0;
  int [ ]         stack      = new int [ 0x10 ];
  private static final int PROGRAM_STARTS = 0x200, ROWS_CHARACTERS = 0x05;
  
  private KeyCallBack      keyCallBack    = new KeyCallBack( );
  
  public CPU( Chip8 chip8 )
  {
    this.chip8 = chip8;
    this.display = chip8.getDisplay( );
    this.reset( );
    
  }
  
  private void reset( )
  {
    // TODO Auto-generated method stub
    
    this.display.clear( );
    
    this.memory = new byte [ 0x1000 ];
    this.registers = new byte [ 0x10 ];
    this.stack = new int [ 0x10 ];
    this.stackPoint = 0;
    this.programCounter = CPU.PROGRAM_STARTS;
    this.address = 0;
    this.delayTimer = 0;
    this.soundTimer = 0;
    
    int count = 0;
    final int [ ] FONTCHARS = { 0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
        0x20, 0x60, 0x20, 0x20, 0x70, // 1
        0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
        0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
        0x90, 0x90, 0xF0, 0x10, 0x10, // 4
        0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
        0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
        0xF0, 0x10, 0x20, 0x40, 0x40, // 7
        0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
        0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
        0xF0, 0x90, 0xF0, 0x90, 0x90, // A
        0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
        0xF0, 0x80, 0x80, 0x80, 0xF0, // C
        0xE0, 0x90, 0x90, 0x90, 0xE0, // D
        0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
        0xF0, 0x80, 0xF0, 0x80, 0x80 // F
    };
    
    for( count = 0; count < this.memory.length; )
      if( count < FONTCHARS.length )
      {
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
        this.memory [ count ] = ( byte ) FONTCHARS [ count++ ];
      } else
      {
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
        this.memory [ count++ ] = 0;
      }
    
    if( this.romData != null )
      for( count = 0; count < this.romData.length; count++ )
      {
        this.romData [ count ] = this.romData [ count ];
        this.memory [ CPU.PROGRAM_STARTS + count ] = this.memory [ CPU.PROGRAM_STARTS
            + count ];
        this.memory [ CPU.PROGRAM_STARTS + count ] = this.romData [ count ];
      }
    
  }
  
  public void loadROM( byte [ ] romData )
  {
    this.romData = romData.clone( );
    this.reset( );
  }
  
  public void handleTimers( )
  {
    if( this.delayTimer > 0 )
      this.delayTimer-- ;
    
    if( --this.soundTimer <= 0 )
    {
      this.display.beep( );
      this.soundTimer = 0;
    }
    
  }
  
  public void emulate( )
  {
    if( this.romData == null )
      this.chip8.stop( );
    
    int opCode = ( this.memory [ this.programCounter ] & Chip8.MASK_8BITS ) << 8
        | this.memory [ this.programCounter + 1 ] & Chip8.MASK_8BITS;
    // this.debug( opCode );
    int x = ( opCode & 0x0F00 ) >> 8; // X index of Vector.
    int y = ( opCode & 0x00F0 ) >> 4; // Y index of Vector.
    int addr = opCode & 0x0FFF; // Address param of opCode.
    int value = opCode & 0x00FF; // Value param opCode.
    this.address &= 0xFFFF; // I register 16 bits.
    
    this.programCounter += 2;
    this.programCounter &= 0x0FFF;
    
    switch( opCode & 0xF000 )
    {
    
      case 0x0000: // 0x0NNN
        switch( opCode & 0x00FF )
        {
        
          case 0x00E0: // CLS
            this.display.clear( );
            this.chip8.setDrawFlag( );
            break;
          
          case 0x00EE: // RET
            this.programCounter = this.stack [ --this.stackPoint ];
            break;
        
        }
        break;
      
      case 0x1000: // JP addr
        this.programCounter = addr;
        break;
      
      case 0x2000: // CALL addr
        this.stack [ this.stackPoint++ ] = this.programCounter;
        this.programCounter = addr;
        break;
      
      case 0x3000: // SE Vx, byte
        if( ( this.registers [ x ] & Chip8.MASK_8BITS ) == value )
          this.programCounter += 2;
        break;
      
      case 0x4000: // SNE Vx, byte
        if( ( this.registers [ x ] & Chip8.MASK_8BITS ) != value )
          this.programCounter += 2;
        break;
      
      case 0x5000: // SE Vx, Vy
        if( ( this.registers [ x ] & Chip8.MASK_8BITS ) == ( this.registers [ y ] & Chip8.MASK_8BITS ) )
          this.programCounter += 2;
        break;
      
      case 0x6000: // LD Vx, byte
        this.registers [ x ] = ( byte ) value;
        break;
      
      case 0x7000: // ADD Vx, byte
        this.registers [ x ] += value;
        break;
      
      case 0x8000: // 0x8000
        switch( opCode & 0x000F )
        {
        // 0x8XY0
          case 0x0000: // LD Vx, Vy
            this.registers [ x ] = this.registers [ y ];
            break;
          // 0x8XY1
          case 0x0001: // OR Vx, Vy
            this.registers [ x ] |= this.registers [ y ];
            break;
          // 0x8XY2
          case 0x0002: // AND Vx, Vy
            this.registers [ x ] &= this.registers [ y ];
            break;
          // 0x8XY3
          case 0x0003: // XOR Vx, Vy
            this.registers [ x ] ^= this.registers [ y ];
            break;
          // 0x8XY4
          case 0x0004: // ADD Vx, Vy
          {
            int result = ( this.registers [ x ] & Chip8.MASK_8BITS )
                + ( this.registers [ y ] & Chip8.MASK_8BITS );
            this.registers [ x ] = ( byte ) result;
            this.registers [ 0xF ] = ( byte ) ( result > 0xFF? 1 : 0 );
            break;
          }
          // 0x8XY5
          case 0x0005: // SUB Vx, Vy
          {
            int result = ( this.registers [ x ] & Chip8.MASK_8BITS )
                - ( this.registers [ y ] & Chip8.MASK_8BITS );
            this.registers [ x ] = ( byte ) result;
            this.registers [ 0xF ] = ( byte ) ( result > 0x0? 1 : 0 );
            break;
          }
          // 0x8XY6
          case 0x0006: // SHR Vx, Vy
            this.registers [ 0xF ] = ( byte ) ( this.registers [ x ]
                & Chip8.MASK_8BITS & 0x01 );
            this.registers [ x ] >>= 1;
            break;
          // 0x8XY7
          case 0x0007: // SUBN Vx, Vy
          {
            int result = ( this.registers [ y ] & Chip8.MASK_8BITS )
                + ( this.registers [ x ] & Chip8.MASK_8BITS );
            this.registers [ x ] = ( byte ) result;
            this.registers [ 0xF ] = ( byte ) ( result > 0x0? 1 : 0 );
            break;
          }
          // 0x8XYE
          case 0x000E: // SHL Vx, Vy
          {
            int result = ( this.registers [ x ] & Chip8.MASK_8BITS ) << 1;
            this.registers [ x ] = ( byte ) ( result & Chip8.MASK_8BITS );
            this.registers [ 0xF ] = ( byte ) ( result > 0xFF? 1 : 0 );
            break;
          }
          
        }
        break;
      
      case 0x9000: // SNE Vx, Vy
        this.programCounter += this.registers [ x ] != this.registers [ y ]? 2
            : 0;
        break; // TODO
        
      case 0xA000: // LD I, addr
        this.address = addr;
        break;
      
      case 0xB000: // JP V0, addr
        this.programCounter = addr + ( this.registers [ 0 ] & Chip8.MASK_8BITS );
        break;
      
      case 0xC000: // RND Vx, byte
        this.registers [ x ] = ( byte ) ( ( int ) Math
            .floor( Math.random( ) * 0xFF ) & value );
        break;
      
      case 0xD000: // DRW Vx, Vy, Count
        ;
        this.registers [ 0xF ] = ( byte ) ( this.display.drawSprite(
            this.registers [ x ] & Chip8.MASK_8BITS,
            this.registers [ y ] & Chip8.MASK_8BITS,
            Arrays.copyOfRange( this.memory, this.address, this.address
                + ( opCode & 0x000F ) ) )? 1 : 0 );
        this.chip8.setDrawFlag( );
        break;
      
      case 0xE000: // 0xE000
        switch( opCode & 0x00FF )
        {
        // 0xEX9E
          case 0x009E: // SKP Vx
            this.programCounter += this.chip8.keyPressed( this.registers [ x ]
                & Chip8.MASK_8BITS )? 2 : 0;
            break;
          // 0xEXA1
          case 0x00A1: // SKNP Vx
            this.programCounter += !this.chip8.keyPressed( this.registers [ x ]
                & Chip8.MASK_8BITS )? 2 : 0;
            break;
        
        }
        break;
      
      case 0xF000: // 0xF0000
        switch( opCode & 0x00FF )
        {
        // 0xFX07
          case 0x0007: // LD Vx, DT
            this.registers [ x ] = ( byte ) this.delayTimer;
            break;
          // 0xFX0A
          case 0x000A: // LD Vx, K
            this.keyCallBack.setX( x );
            this.chip8.waitKey( this.keyCallBack );
            break;
          // 0xFX15
          case 0x0015: // LD DT, Vx
            this.delayTimer = this.registers [ x ] & Chip8.MASK_8BITS;
            break;
          // 0xFX18
          case 0x0018: // LD ST, Vx
            this.soundTimer = this.registers [ x ] & Chip8.MASK_8BITS;
            break;
          // 0xFX1E
          case 0x001E: // ADD I, Vx
            this.address += this.registers [ x ] & Chip8.MASK_8BITS;
            break;
          // 0xFX29
          case 0x0029: // LD F, Vx
            this.address = ( this.registers [ x ] & Chip8.MASK_8BITS )
                * CPU.ROWS_CHARACTERS;
            break;
          // 0xFX33
          case 0x0033: // LD B, Vx
            for( int localCount = 0; localCount < 3; localCount++ )
              this.memory [ this.address + localCount ] = ( byte ) Math
                  .floor( ( this.registers [ x ] & Chip8.MASK_8BITS )
                      / Math.pow( 10, 2 - localCount ) % 10 );
            break;
          // 0xFX55
          case 0x0055: // LD [I], Vx
            for( int localCount = 0; localCount <= x; localCount++ )
              this.memory [ this.address + localCount ] = this.registers [ localCount ];
            break;
          // 0xFX65
          case 0x0065: // LD Vx, [I]
            for( int localCount = 0; localCount <= x; localCount++ )
              this.registers [ localCount ] = this.memory [ this.address
                  + localCount ];
            break;
        
        }
        break;
      
      default:
        throw new RuntimeException( "Unknown opCode "
            + Integer.valueOf( opCode ) + " passed. Terminating." );
    }
    
  }
  
  @SuppressWarnings( "unused" )
  private void debug( int opCode )
  {
    
    System.out.print( "\nCode: " + Integer.toString( opCode, 0x10 ) );
    System.out.print( "\nRegisters: " );
    for( byte b : this.registers )
      System.out.print( b + " " );
    System.out.print( "\nStack: " );
    for( int b : this.stack )
      System.out.print( b + " " );
    System.out.print( "\nStackPoint: " + this.stackPoint );
    try
    {
      Thread.sleep( 200 );
    } catch( InterruptedException e )
    {
      // TODO Auto-generated catch block
      e.printStackTrace( );
    }
    
  }
  
  public class KeyCallBack
  {
    
    private int x = 0;
    
    public KeyCallBack( )
    {
      
    }
    
    public void callBack( byte key )
    {
      CPU.this.registers [ this.x ] = key;
    }
    
    public void setX( int x )
    {
      this.x = x;
    }
    
  }
  
}
