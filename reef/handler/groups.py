import config
import restlite
import urlparse
import authentication

### Allows for submitting groups ###

''' URIs of the form /groups/$ are used for batch handling, whereas
those of the form /groups/somethingMore are used for individual groups'''

# Blank newgroup with some values initialized
def __newGroup() :
  return { "name" : None, "size" : 0, "workloads" : [], "filters" : []}

@restlite.resource
def group_batch_handler():

  ## GET requests to this uri will return a summary of current groups
  def GET(request):
    authentication.login(request)
    groups_summary = {}
    group_data = config.getSettings("groups")
    
    print group_data
    
    for group in group_data:
      try:
        groups_summary[group] = group_data[group]['size']
      except KeyError:
        pass
      
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
    
    group_sizes = urlparse.parse_qs(entity)

    # Validate all groups before we begin
    for group in group_sizes:
      # Make sure each group was only specified once
      if len(group_sizes[group]) != 1:
        raise restlite.Status, "400 Multiple actions for the same method"
      else:
        group_sizes[group] = group_sizes[group][0]

      # Get a correct integer from the size
      try:
        group_sizes[group] = int(group_sizes[group])
      except ValueError:
        raise restlite.Status, "400 Group size must be a positive integer"

      if group_sizes[group] < 0:
        raise restlite.Status, "400 Group size must be a positive integer"

      if group_sizes[group] == 0 and group not in existing_groups:
        raise restlite.Status, "400 Cannot delete non-existent group"

    # Use the input now
    for group in group_sizes:
      if group_sizes[group] == 0:
        # Already checked this exists
        del existing_groups[group]
      else:
        try:
          existing_groups[group]['size'] = group_sizes[group]
        except KeyError:
          existing_groups[group] = __newGroup()
          existing_groups[group]["name"] = group
          existing_groups[group]["size"] = group_sizes[group]
      
    # Tell them the new info
    return GET(request)
    
  return locals()

@restlite.resource
def group_handler():
  
  ## GET requests are for grabbing current info on a group
  def GET(request):
    authentication.login(request)
   
    # To find info on a group call /groups/groupName
    group_name = request['PATH_INFO']

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
    group_name = request['PATH_INFO']

    args = urlparse.parse_qs(request['QUERY_STRING'])

    g_data = _getGroupDataFromArgs(args)

    if group_name in existing_groups:
      if "size" in g_data and g_data == 0:
        # Delete the group
        del existing_groups[group_name]
      else:
        existing_groups[group_name].update(g_data)
    else:
      # New group
      n_group = __newGroup()
      n_group["name"] = group_name
      n_group.update(g_data)
      if n_group["size"] != 0:
        existing_groups[group_name] = n_group
    
    return GET(request)

  return locals()


# Returns a dict of new data gotten from the given args
# Raises a status exception if args are incorrect
def _getGroupDataFromArgs(args):
  group = {}

  # Get a size value
  if "size" in args:
    try:
      group["size"] = int(args["size"][0])
    except ValueError:
      raise restlite.Status, "400 Size must be integer"
    del args["size"]

  # Add workload list
  if "workloads" in args:
    group["workloads"] = args["workloads"]
    del args["workloads"]

  # Add filter list
  if "filters" in args:
    group["workloads"] = args["filters"]
    del args["filters"]

  if len(args) > 0:
    raise restlite.Status, "400 Unrecognised Arguments"

  return group
