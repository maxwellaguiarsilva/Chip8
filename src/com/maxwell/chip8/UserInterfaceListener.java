package com.maxwell.chip8;

public interface UserInterfaceListener
{
  
  public void onKeyDown( byte keyNumber );
  
  public void onKeyUp( byte keyNumber );
  
  public void onLoadROM( byte [ ] romData );
  
}
