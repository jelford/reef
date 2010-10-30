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
      handler.handle(sections.getContent(), 200, "OK");
    }
  }

  private void refreshSections(final RequestHandler<SectionList> handler) {
    EasyRequest request = new EasyRequest() {
      @Override
      protected void requested(Integer code, String reason, String content) {
        SectionList list = null;
        if (code != null && code == 200) {
          list = new SectionList(content);
          if (sections == null)
            sections = new UpdateInfo<SectionList>(list, code, reason);
          else
            sections.update(list, code, reason);
        }
        handler.handle(list, code, reason);
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
      handler.handle(settings.get(key).getContent(), 200, "OK");
    }
  }

  private void refreshSettings(final String key,
      final RequestHandler<SettingGroup> handler) {
    EasyRequest request = new EasyRequest() {
      @Override
      protected void requested(Integer code, String reason, String content) {
        SettingGroup grp = null;
        if (code != null && code == 200) {
          grp = new SettingGroup(content);
          if (settings.containsKey(key))
            settings.get(key).update(grp, code, reason);
          else
            settings.put(key, new UpdateInfo<SettingGroup>(grp, code, reason));
        }
        handler.handle(grp, code, reason);
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
      public void handle(SettingGroup reply, Integer code, String msg) {
        Setting stn = null;
        if(reply != null && reply.contains(key))
          stn = reply.get(key);

        if (code != null && code == 200)
          handler.handle(stn, code, msg);
      }
    };
    
    getSettings(section, grpHandler, oldTime);
  }

  // Used as the callback object for any requests
  // Give reply, response code and response message whenever we have a definite response
  // If there is an error response code and reason are given
  // If there is no response, then code is null
  // When we request cached data, if it is still in date we get a positive response
  public interface RequestHandler<Type> {
    public void handle(Type reply, Integer code, String msg);
  }

  private class UpdateInfo<Type> {
    private Type content;
    private Date lastUpdate;
    private Date lastSuccessfulUpdate;
    //private Integer lastCode;
    //private String lastReason;

    public UpdateInfo(Type type, Integer code, String reason) {
      update(type, code, reason);
    }

    public void update(Type content, Integer code, String reason) {
      this.content = content;
      //this.lastCode = code;
      //this.lastReason = reason;
      this.lastUpdate = new Date();

      if (code != null && code == 200)
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
