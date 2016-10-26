package com.technology.jep.jepria.auto.model.module;

import com.technology.jep.jepria.auto.JepRiaModuleAuto;

public class Module {

  public Module(String moduleID, String entranceURL, JepRiaModuleAuto moduleAuto) {
    super();
    this.moduleID = moduleID;
    this.entranceURL = entranceURL;
    this.moduleAuto = moduleAuto;
  }

  private String moduleID;
  private String entranceURL;
  private JepRiaModuleAuto moduleAuto;
  
  /**
   * @return the moduleAuto
   */
  public JepRiaModuleAuto getModuleAuto() {
    return moduleAuto;
  }

  /**
   * @return the moduleID
   */
  public String getModuleID() {
    return moduleID;
  }

  /**
   * @return the entraceURL
   */
  public String getEntranceURL() {
    return entranceURL;
  }
}
