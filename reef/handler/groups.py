import handlers
import config
import restlite

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
    argument_list = {}
    variables = entity.split("&")
    for variable in variables:
      try:
        key, value = variable.split("=")
        argument_list[key] = value
      except ValueError:
        print("Had a problem handling argument: " + variable)
        pass
    
    '''
    Here the server must take care of setting up the group info.
    What do we need to know?
    name
    size
    group_number
    workloads
    '''
    groups = config.getSettings("groups")
    
    for group_name in argument_list:
      new_group = {
        "name" : group_name,
        "size" : int(argument_list[group_name])
      }
      
      if new_group["size"] == 0:
        try:
          del groups[group_name]
        except KeyError:
          #User tried to delete a group that doesn't exist. Mneh
          pass
        continue
      
      try:
        old_group = groups[group_name]
      except KeyError:
        # If we haven't got the group already, create a new one
        # and add it to the list.
        groups_before_this_one = len(filter(lambda g: g < group_name, groups))
        old_group = {
          "group_number" : groups_before_this_one,
          "workloads" : {},
        }
        groups[group_name] = old_group
      
      # In either case, update the "old" group inplace in the list
      old_group.update(new_group)
    
    # Tell them the new info
    return GET(request)
    
  return locals()

@restlite.resource
def group_handler():
  ## GET requests are for grabbing current info on a group
  def GET(request):
    handlers.login(request)
    # To find info on a group call /groups/groupName (note no trailing slash)
    group_settings = {}
    group_name = request['PATH_INFO']
    try:
      group_settings = config.getSettings("groups",True)[group_name]
      return request.response(group_settings)
    # Return 404 if they use an invalid group name
    except KeyError:
      raise restlite.Status, "404 Group Not Found"
    
  # POST requests will set up a group
  def POST(request,entity):
    pass
  
  return locals()