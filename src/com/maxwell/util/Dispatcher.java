package com.maxwell.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Dispatcher< TListenerType > implements Listenable< TListenerType >
{
  
  List< TListenerType > listenerSet = new ArrayList< TListenerType >( );
  
  public Dispatcher( )
  {
    
  }
  
  @Override
  public void addListener( TListenerType listener )
  {
    this.listenerSet.add( listener );
  }
  
  @Override
  public void removeListener( TListenerType listener )
  {
    this.listenerSet.remove( listener );
  }
  
  public void dispatch( String eventName, Object [ ] args )
  {
    
    for( TListenerType listener : this.listenerSet )
      try
      {
        Method [ ] methodSet = listener.getClass( ).getDeclaredMethods( );
        METHODLOOP: for( Method method : methodSet )
          if( method.getName( ).equals( eventName ) )
          {
            method.setAccessible( true );
            method.invoke( listener, args );
            break METHODLOOP;
          }
      } catch( IllegalAccessException e )
      {
        e.printStackTrace( );
      } catch( InvocationTargetException e )
      {
        e.printStackTrace( );
      }
  }
  
  public void dispatch( String eventName, Object [ ] args,
      Class< ? > [ ] argsClassSet )
  {
    
    for( TListenerType listener : this.listenerSet )
      try
      {
        Method method = listener.getClass( ).getDeclaredMethod( eventName,
            argsClassSet );
        method.setAccessible( true );
        method.invoke( listener, args );
      } catch( NoSuchMethodException e )
      {
        e.printStackTrace( );
      } catch( IllegalAccessException e )
      {
        e.printStackTrace( );
      } catch( InvocationTargetException e )
      {
        e.printStackTrace( );
      }
  }
  
}