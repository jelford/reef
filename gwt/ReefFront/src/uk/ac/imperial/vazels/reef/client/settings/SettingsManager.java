package uk.ac.imperial.vazels.reef.client.settings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.EasyRequest;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SectionList;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

public class SettingsManager {
  
  // Singleton Schizzlement
  
  private static SettingsManager manager = null;
  
  public static SettingsManager getManager()
  {
    if(manager == null)
      manager = new SettingsManager();
    return manager;
  }
  
  // Cached settings
  private UpdateInfo<SectionList> sections;
  private Map<String, UpdateInfo<SettingGroup>> settings;

  public SettingsManager() {
    settings = new HashMap<String, UpdateInfo<SettingGroup>>();
  }

  public void getSections(RequestHandler<SectionList> handler) {
    getSections(handler, 0);
  }

  // Get sectionlist, if they were refreshed more than oldTime minutes ago
  // they will be updated first
  public void getSections(RequestHandler<SectionList> handler, int oldTime) {
    if (sections == null || !sections.lessOldThan(oldTime)) {
      refreshSections(handler);
    } else {
      // We have a recent successful update,
      // so pretend it's ok whatever the last request was
      handler.handle(sections.getContent(), true, "200 OK");
    }
  }

  private void refreshSections(final RequestHandler<SectionList> handler) {
    EasyRequest request = new EasyRequest() {
      @Override
      protected void requested(Integer code, String reason, String content) {
        SectionList list = null;
        if (isSuccess(code)) {
          list = new SectionList(content);
          if (sections == null)
            sections = new UpdateInfo<SectionList>(list, true);
          else
            sections.update(list, true);
        }
        
        handler.handle(list, isSuccess(code), getMessage(code, reason));
      }
    };

    request.request(RequestBuilder.GET, "/settings/", null);
  }

  public void getSettings(String key, RequestHandler<SettingGroup> handler) {
    getSettings(key, handler, 0);
  }

  // Get settingsgrp, if they were refreshed more than oldTime minutes ago
  // they will be updated first
  public void getSettings(String key, RequestHandler<SettingGroup> handler,
      int oldTime) {
    if (!settings.containsKey(key) || !settings.get(key).lessOldThan(oldTime)) {
      refreshSettings(key, handler);
    } else {
      // We have a recent successful update,
      // so pretend it's ok whatever the last request was
      handler.handle(settings.get(key).getContent(), true, "200 OK");
    }
  }

  private void refreshSettings(final String key,
      final RequestHandler<SettingGroup> handler) {
    EasyRequest request = new EasyRequest() {
      @Override
      protected void requested(Integer code, String reason, String content) {
        SettingGroup grp = null;
        if (isSuccess(code)) {
          grp = new SettingGroup(content);
          if (settings.containsKey(key))
            settings.get(key).update(grp, true);
          else
            settings.put(key, new UpdateInfo<SettingGroup>(grp, true));
        }
        handler.handle(grp, isSuccess(code), getMessage(code, reason));
      }
    };

    request.request(RequestBuilder.GET, "/settings/" + key, null);
  }

  public void getSetting(String section, String key,
      RequestHandler<Setting> handler) {
    getSetting(section, key, handler, 0);
  }

  public void getSetting(String section, final String key,
      final RequestHandler<Setting> handler, int oldTime) {
    
    // Handler to wrap the handler we're given
    RequestHandler<SettingGroup> grpHandler = new RequestHandler<SettingGroup>() {
      @Override
      public void handle(SettingGroup reply, boolean success, String reason) {
        if(success){
          if(reply != null && reply.contains(key)){
            Setting stn = reply.get(key);
            handler.handle(stn, true, "200 OK");
          }
          else
            handler.handle(null, false, "Setting does not exist.");
        }
        else
          handler.handle(null, false, reason);
      }
    };
    
    getSettings(section, grpHandler, oldTime);
  }
  
  // Useful functions
  private String getMessage(Integer code, String reason){
    if(code != null && reason != null)
      return code + " " + reason;
    else if(code != null)
      return code.toString();
    else if(reason != null)
      return reason;
    else
      return "Error in request";
  }
  
  private boolean isSuccess(Integer code){
    return (code != null && code == 200);
  }

  // Used as the callback object for any requests
  // Give reply, response code and response message whenever we have a definite response
  // If there is an error response code and reason are given
  // If there is no response, then code is null
  // When we request cached data, if it is still in date we get a positive response
  public interface RequestHandler<Type> {
    public void handle(Type reply, boolean success, String message);
  }

  private class UpdateInfo<Type> {
    private Type content;
    private Date lastUpdate;
    private Date lastSuccessfulUpdate;

    public UpdateInfo(Type type, boolean success) {
      update(type, success);
    }

    public void update(Type content, boolean success) {
      this.content = content;
      this.lastUpdate = new Date();

      if (success)
        this.lastSuccessfulUpdate = this.lastUpdate;
    }

    public boolean lessOldThan(int minutes) {
      Date now = new Date();
      long millisOld = now.getTime() - lastSuccessfulUpdate.getTime();
      return (millisOld < minutes * 60 * 1000);
    }

    public Type getContent() {
      return content;
    }
  }
}
