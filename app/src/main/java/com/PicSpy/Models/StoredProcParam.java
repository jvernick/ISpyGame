package com.picspy.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * TODO document this.
 * Modified the calss from the dreamfactory model to only have the required fields
 */
public class StoredProcParam {
  /* Name of the parameter, required for OUT and INOUT types, must be the same as the stored procedure's parameter name. */
  @JsonProperty("name")
  private String name;
  /* Value of the parameter, used for the IN and INOUT types, defaults to NULL. */
  @JsonProperty("value")
  private int value;

  public StoredProcParam(String name, int value) {
    this.name = name;
    this.value = value;
  }
  @Override
  public String toString()  {
    return "FriendsRecord {" +
            "\n" + "  name: " + name +
            "\n" + "  value: " + value +
            "\n}";
  }
}

