package com.picspy.models;

/**
 * Created by BrunelAmC on 6/7/2015.
 */
 /*TODO remove class: This is just for demosntration*/
/*TODO Set default vaues for un-set values in model*/

import com.fasterxml.jackson.annotation.JsonProperty;

public class RegisterModel {
    @JsonProperty("email")
    private String email = null;
    @JsonProperty("first_name")
    private String first_name = null;
    @JsonProperty("last_name")
    private String last_name = null;
    @JsonProperty("display_name")
    private String display_name = null;
    @JsonProperty("new_password")
    private String new_password = null;
    @JsonProperty("code")
    private String code = null;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getNew_password() {
        return new_password;
    }

    public void setNew_password(String new_password) {
        this.new_password = new_password;
    }

    /*
    public String getFirst_name() {
         return first_name;
     }

     public void setFirst_name(String first_name) {
         this.first_name = first_name;
     }

     public String getLast_name() {
         return last_name;
     }

     public void setLast_name(String last_name) {
         this.last_name = last_name;
     }

     public String getCode() {
         return code;
     }

     public void setCode(String code) {
         this.code = code;
     }

     */
    @Override
    /* Requires string format below with each needed parameter defined*/
    public String toString() {
        return "class RegisterModel {\n" + " email: " + email + "\n" + " new_password: " +
                new_password + "\n" + " display_name: " + display_name + "\n" + "}\n";
    }
}
