package uk.ac.imperial.vazels.reef.client.settings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.RequestBuilder;

import uk.ac.imperial.vazels.reef.client.EasyRequest;
import uk.ac.imperial.vazels.reef.client.MultipleRequester;
import uk.ac.imperial.vazels.reef.client.RequestHandler;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SectionList;
import uk.ac.imperial.vazels.reef.client.settings.overlay.Setting;
import uk.ac.imperial.vazels.reef.client.settings.overlay.SettingGroup;

public class SettingsManager {

  // --------- Singleton Schizzlement ----------

  private static SettingsManager manager = null;

  // Get singleton manager
  public static SettingsManager getManager() {
    if (manager == null)
      manager = new SettingsManager();
    return manager;
  }

  // ---------- Gettings settings -------------

  // Cached settings
  private UpdateInfo<SectionList> sections;
  private Map<String, UpdateInfo<SettingGroup>> settings;

  public SettingsManager() {
    settings = new HashMap<String, UpdateInfo<SettingGroup>>();
  }

  // Grab and save sections
  public void getSectionsNow(RequestHandler<SectionList> handler) {
    new SectionRequest().go(handler);
  }

  // Grab cached sections, only request fresh if they are not here
  public void getSections(RequestHandler<SectionList> handler) {
    if (sections == null)
      getSectionsNow(handler);
    else
      handler.handle(sections.getContent());
  }

  // Get sectionlist, if they were refreshed more than oldTime minutes ago
  // they will be updated first
  public void getSections(RequestHandler<SectionList> handler, int oldTime) {
    if (sections == null || sections.olderThan(oldTime))
      getSectionsNow(handler);
    else
      getSections(handler);
  }

  
  //Easy request to grab the sections
  private class SectionRequest extends MultipleRequester<SectionList> {
    public SectionRequest(){
      super(RequestBuilder.GET, "/settings/", new Converter<SectionList>() {
        @Override
        public SectionList convert(String original) {
          return new SectionList(original);
        }
      });
    }
    
    public void received(SectionList reply, boolean success, String message){
      if(success){
        if (sections == null)
          sections = new UpdateInfo<SectionList>(reply);
        else
          sections.update(reply);
      }
    }
  }

  // Grab and save a specific section
  public void getSettingsNow(String key, RequestHandler<SettingGroup> handler) {
    new SettingsRequest(key).go(handler);
  }

  // Get cached settings, only request fresh if there is no cache
  public void getSettings(String key, RequestHandler<SettingGroup> handler) {
    if (!settings.containsKey(key))
      getSettingsNow(key, handler);
    else
      handler.handle(settings.get(key).getContent());
  }

  // Get settingsgrp, if they were refreshed more than oldTime minutes ago
  // they will be updated first
  public void getSettings(String key, RequestHandler<SettingGroup> handler,
      int oldTime) {
    if (!settings.containsKey(key) || settings.get(key).olderThan(oldTime))
      getSettingsNow(key, handler);
    else
      getSettings(key, handler);
  }

  // Easy request to grab a specific setting section
  private class SettingsRequest extends MultipleRequester<SettingGroup> {
    private final String key;
    
    public SettingsRequest(String key){
      super(RequestBuilder.GET, "/settings/"+key, new Converter<SettingGroup>() {
        @Override
        public SettingGroup convert(String original) {
          return new SettingGroup(original);
        }
      });
      
      this.key = key;
    }
    
    public void received(SettingGroup reply, boolean success, String message){
      if(success){
        if (settings.containsKey(key))
          settings.get(key).update(reply);
        else
          settings.put(key, new UpdateInfo<SettingGroup>(reply));
      }
    }
  }

  public void getSettingNow(String section, final String key,
      final RequestHandler<Setting> handler) {
    getSettingsNow(section, new RequestHandler<SettingGroup>() {
      @Override
      public void handle(SettingGroup reply, boolean success, String message) {
        if (success) {
          if (reply != null && reply.contains(key))
            handler.handle(reply.get(key));
          else
            handler.handle(null, false, "Section does not contain setting.");
        } else {
          handler.handle(null, false, message);
        }
      }
    });
  }

  // Grab cached settings, only get fresh if none are cached
  public void getSetting(String section, String key,
      RequestHandler<Setting> handler) {
    if (!settings.containsKey(section)
        || !settings.get(section).getContent().contains(key))
      getSettingNow(section, key, handler);
    else
      handler.handle(settings.get(section).getContent().get(key));
  }

  // Get setting, if it was refreshed more than oldTime minutes ago
  // it will be updated first
  public void getSetting(String section, final String key,
      final RequestHandler<Setting> handler, int oldTime) {

    if(!settings.containsKey(section) || settings.get(section).olderThan(oldTime))
      getSettingNow(section, key, handler);
    else
      getSetting(section, key, handler);
  }

  // --------------- Useful functions -----------------

  // Nice way to store the update times for data
  private class UpdateInfo<Type> {
    private Type content;
    private Date lastUpdate;

    public UpdateInfo(Type type) {
      update(type);
    }

    public void update(Type content) {
      this.content = content;
      this.lastUpdate = new Date();
    }

    public boolean olderThan(int minutes) {
      Date now = new Date();
      long millisOld = now.getTime() - lastUpdate.getTime();
      return (millisOld > minutes * 60 * 1000);
    }

    public boolean olderThan(Date then) {
      return lastUpdate.getTime() < then.getTime();
    }
    
    @SuppressWarnings("unused")
    public boolean olderThan(UpdateInfo<Type> then) {
      return olderThan(then.lastUpdate);
    }
    
    public Type getContent() {
      return content;
    }
  }

  // ------------- Setting Settings ------------------

  private Map<String, EditRequest> sectionChanges = new HashMap<String, EditRequest>(); // Changes to make
  
  // Add a change to make
  public void addChange(String section, PendingChange change) {
    if(change == null)
      return;
    
    if(!sectionChanges.containsKey(section))
      sectionChanges.put(section, new EditRequest(section));
    
    sectionChanges.get(section).add(change);
  }

  // commit all changes, every single one will invoke the handler
  // do not remove the changes afterward
  public void commitChanges(RequestHandler<SettingGroup> handler) {
    for(String section : sectionChanges.keySet())
      sectionChanges.get(section).go(handler);
  }
  
  public void clearChanges() {
    sectionChanges = new HashMap<String, EditRequest>();
  }

  private class EditRequest extends MultipleRequester<SettingGroup> {
    // Saves changes along with the time they were
    private Map<String, PendingChange> changes;
    private String key;

    public EditRequest(String key) {
      super(RequestBuilder.POST, "/settings/"+key, new Converter<SettingGroup>(){
        @Override
        public SettingGroup convert(String original) {
          return new SettingGroup(original);
        }
      });
      
      changes = new HashMap<String, PendingChange>();
      this.key = key;
    }
    
    public void add(PendingChange change){
      changes.put(change.setting(), change);
    }
    
    protected QueryArg[] getArgs(){
      QueryArg[] args = new QueryArg[changes.size()];
      int i=0;
      for(QueryArg arg : changes.values()){
        args[i] = arg;
        i++;
      }
      return args;
    }
    
    protected void received(SettingGroup reply, boolean success, String message){
      // Update local settings if post is received
      if(success){
        if (settings.containsKey(key))
          settings.get(key).update(reply);
        else
          settings.put(key, new UpdateInfo<SettingGroup>(reply));
      }
    }
  }
  
  public static abstract class PendingChange extends EasyRequest.QueryArg {
    private final String setting;

    public PendingChange(String setting) {
      super("", "");
      if(setting == null)
        throw new NullPointerException();
      this.setting = setting;
    }

    // Derived classes override this to choose how it will be displayed in the
    // request
    public abstract String getName();
    public abstract String getValue();

    public String setting() {
      return setting;
    }
  }

  public static class PendingDeletion extends PendingChange {
    public PendingDeletion(String setting) {
      super(setting);
    }

    @Override
    public String getName() {
      return this.setting() + "_r";
    }

    @Override
    public String getValue() {
      return null;
    }
  }

  public static class PendingInteger extends PendingChange {
    private Integer value;

    public PendingInteger(String setting, Integer value) {
      super(setting);
      if (value == null)
        throw new NullPointerException();
      this.value = value;
    }

    @Override
    public String getName() {
      return this.setting() + "_i";
    }

    @Override
    public String getValue() {
      return value.toString();
    }
  }

  public static class PendingDouble extends PendingChange {
    private Double value;

    public PendingDouble(String setting, Double value) {
      super(setting);
      if (value == null)
        throw new NullPointerException();
      this.value = value;
    }

    @Override
    public String getName() {
      return this.setting() + "_d";
    }

    @Override
    public String getValue() {
      return value.toString();
    }
  }

  public static class PendingString extends PendingChange {
    private String value;

    public PendingString(String setting, String value) {
      super(setting);
      if (value == null)
        throw new NullPointerException();
      this.value = value;
    }

    @Override
    public String getName() {
      return this.setting() + "_s";
    }

    @Override
    public String getValue() {
      return value;
    }
  }
}
