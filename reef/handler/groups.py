import handlers
import config
import restlite

### Allows for submitting groups ###

''' URIs of the form /groups/$ are used for batch handling, whereas
those of the form /groups/somethingMore are used for individual groups'''

### Allows for submitting groups ###

''' URIs of the form /groups/$ are used for batch handling, whereas
those of the form /groups/somethingMore are used for individual groups'''

@restlite.resource
def group_batch_handler():

  ## GET requests to this uri will return a summary of current groups
  def GET(request):
    handlers.login(request)
    groups_summary = {}
    group_data = config.getSettings("groups")
    for group in group_data:
      try:
        groups_summary[group] = group_data[group]['size']
      except KeyError:
        pass
  
    #print("Returning a dummy groups content dictionary")
    #groups_summary = {"group1":1,"group2":3}
    return request.response(groups_summary)
    
  def POST(request,entity):
    handlers.login(request)
      
    '''
    Here the server must take care of setting up the group info.
    What do we need to know?
    name <-- need to be given this by the post request
    size <-- need to be given this by the post request
    group_number <-- calculated when the server starts
    workloads <-- default value: {}
    filters <-- given to us in a specific call later, default value: []
    '''
    
    existing_groups = config.getSettings("groups")
    
    argument_list = {}
    variables = entity.split("&")
    for variable in variables:
      try:
        name, size = variable.split("=")
        new_group = {"name" : name, "size": int(size)}
        if name in existing_groups:
          existing_groups[name].update(new_group)
        else:
          new_group.update({"workloads" : {}, "filters" : []})
          existing_groups[name] = new_group
        print existing_groups[name]
      except ValueError:
        print("Had a problem handling argument: " + variable + \
               ": Argument list may have been improperly formatted; should "\
               "be in the form '.../group_name=some_size&other_name=other_size&...'"\
               " where all size values are integers.")
        continue
      
    # Tell them the new info
    return GET(request)
    
  return locals()

@restlite.resource
def group_handler():
  
  ## GET requests are for grabbing current info on a group
  def GET(request):
    handlers.login(request)
    # To find info on a group call /groups/groupName
    group_name = request['PATH_INFO'].split('/')[0]
    try:
      group_settings = config.getSettings("groups",True)[group_name]
      return request.response(group_settings)
    # Return 404 if they use an invalid group name
    except KeyError:
      raise restlite.Status, "404 Group Not Found"
    
  # POST requests will set up a group
  def POST(request,entity):
    handlers.login(request)
      
      existing_groups = config.getSettings("groups")
      
      try:
        group_name, arguments = request['PATH_INFO'].split('/')[0]
        if group_name in existing_groups:
          group_settings = existing_groups[group_name]
        else:
          #TODO: must extract group name
          #new_group.update({"workloads" : {}, "filters" : []})
          #existing_groups[name] = new_group
          pass
      except ValueError:
        raise restlite.Status, "400 Must specify some arguments when making " + \
                                "POST requests on groups"
      
      for arg in arguments.split("&"):
        try:
          key, value = arg.split("=")
        except ValueError:
          raise restlite.Status, "400 When setting group attributes must give " + \
                                  "a list in the form: arg1=val1&arg2=val2 ..."
        
        if key == "filter":
          group_settings["filters"].append(value)
  
  return locals()
