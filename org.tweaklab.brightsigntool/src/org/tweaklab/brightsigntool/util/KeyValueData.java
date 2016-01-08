package org.tweaklab.brightsigntool.util;

public class KeyValueData {

  public String key;
  public String value;
  
  public KeyValueData(){
    
  }
  
  public KeyValueData(String key, String value){
    this.key = key;
    this.value = value;
    
  }
  
  @Override
  public String toString(){
    return key;
  }
}
