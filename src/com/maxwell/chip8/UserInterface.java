package com.maxwell.chip8;

import com.maxwell.util.Listenable;

public interface UserInterface extends Listenable< UserInterfaceListener >
{
  
  public void updateDisplay( byte [ ] display );
  
  public void beep( );
  
}