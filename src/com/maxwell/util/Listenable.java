package com.maxwell.util; 
 
public interface Listenable < TListenerType >
{
  public void addListener( TListenerType listener ); 
  public void removeListener( TListenerType listener ); 
}