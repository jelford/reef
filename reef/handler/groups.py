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
    group_data = config.getSettings("groups").keys()
    
    return request.response(group_data)
    
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

    args = urlparse.parse_qs(entity)

    g_data = _getGroupDataFromArgs(args)

    if group_name in existing_groups:
      if "size" in g_data and g_data["size"] == 0:
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
    
    #print config.getSettings

    try:
      return request.response(existing_groups[group_name])
    except KeyError:
      return request.response({"name":group_name, "size":0})

    # Cannot do as we are not sure the group exists
    #return GET(request)

  return locals()


# Returns a dict of new data gotten from the given args
# Raises a status exception if args are incorrect
def _getGroupDataFromArgs(args):
  group = {}
  n_args = len(args)

  # Get a size value
  if "size" in args:
    try:
      group["size"] = int(args["size"][0])
    except ValueError:
      raise restlite.Status, "400 Size must be integer"
    n_args = n_args - 1

  # Add workload list
  if "workloads" in args:
    group["workloads"] = args["workloads"]
    n_args = n_args - 1

  # Add filter list
  if "filters" in args:
    group["workloads"] = args["filters"]
    n_args = n_args - 1

  if n_args > 0:
    raise restlite.Status, "400 Unrecognised Arguments"

  return group
