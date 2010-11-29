import config
import restlite
import urlparse
import authentication

### Allows for submitting groups ###

''' URIs of the form /groups/$ are used for batch handling, whereas
those of the form /groups/somethingMore are used for individual groups'''

# Blank newgroup with some values initialized
def __getNewGroup():
  return {"workloads" : [], "filters" : [], "sue_components" : []}

@restlite.resource
def group_batch_handler():

  ## GET requests to this uri will return a summary of current groups
  def GET(request):
    authentication.login(request)
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
    authentication.login(request)
      
    '''
    Here the server must take care of setting up the group info.
    What do we need to know?
    name <-- need to be given this by the post request
    size <-- need to be given this by the post request
    group_number <-- calculated when the server starts
    workloads <-- default value: [] (a list of workload names applied to the group)
    filters <-- given to us in a specific call later, default value: []
    '''
    
    existing_groups = config.getSettings("groups")
    
    argument_list = {}
    variables = entity.split("&")
    for variable in variables:
      try:
        name, size = variable.split("=")
        new_group = {"name" : name, "size": int(size)}
        if int(size) == 0:
          del(existing_groups[name])
          continue
        if name in existing_groups:
          existing_groups[name].update(new_group)
        else:
          new_group.update(__getNewGroup())
          existing_groups[name] = new_group
      except ValueError:
        print("Had a problem handling argument: " + variable + \
               ": Argument list may have been improperly formatted; should "\
               "be in the form '.../group_name=some_size&other_name=other_size&...'"\
               " where all size values are integers.")
        continue
      except KeyError:
        # Probably we tried to delete a group we've already dealt with
        print("Tried to delete a group that's already been deleted")
        continue
      
    # Tell them the new info
    return GET(request)
    
  return locals()

@restlite.resource
def group_handler():
  
  ## GET requests are for grabbing current info on a group
  def GET(request):
    authentication.login(request)
   
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
    authentication.login(request)

    existing_groups = config.getSettings("groups")
    try:
      group_name, arg_string = request['PATH_INFO'].split('/')
    except ValueError:
      raise reslite.Status, "400 Group POST requests should be of the form:"+ \
                              ".../group_name/args1=1&arg2=2&arg3=3..."
    args = urlparse.parse_qs(arg_string)
    
    ## Clear this up so it fits with server-side group representation
    # Can't rename a group TODO: Or can we?
    args["name"] = group_name
    # Single-valued lists are single values
    for arg in args:
      if arg == 'filters' : continue # Actually, single filter lists are fine
      elif arg == 'workloads' : continue # Likewise with workloads
      if len(args[arg]) == 1:
        args[arg] = args[arg][0]
    # Size is an integer
    if "size" in args:
      args["size"] = int(args["size"])
    
    
    # Deal with the case that this is the first time we've accessed the group
    existing_groups.setdefault(group_name, getNewGroup())
  
    # Update the group with all the information passed in by the client
    existing_groups[group_name].update(args)
    
    # Let's compensate for bad design by clearing up at the end
    try:
      if existing_groups[group_name]["size"] == 0:
        del(existing_groups[group_name])
    except KeyError:
      del(existing_groups[group_name])
    
    
    return GET(request)

  return locals()
