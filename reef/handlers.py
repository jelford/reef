import restlite
import config
import os
from vazelsmanager import runVazels, stopVazels

authmodel = None

def setAuth(model):
    global authmodel
    authmodel = model

def getRouting():
    return [
        #(r'GET,POST /settings$', 'GET,POST /settings/'),
        #(r'GET,POST /settings/', settings_editor),
        (r'GET,POST /testing/', test_handler),
        (r'GET,POST /groups/$', group_batch_handler),
        (r'GET,POST /groups/', group_handler),
        (r'GET,POST /', auth_page_handler)
        (r'POST /control/start/?$', start_handler),
        (r'POST /control/stop/?$', stop_handler),
        (r'GET,POST /', auth_page_handler),
    ]


### Serves 404 not found ###

@restlite.resource
def generic_page_handler():
    global authmodel
    def GET(request):
        #if authmodel:
        #    authmodel.login(request)
        #return request.response(('path', request['PATH_INFO'], request['SCRIPT_NAME']))
        raise restlite.Status, "404 Not Found"
    def POST(request,entity):
      raise restlite.Status, "405, could not handle generic post request"
    return locals()


### Manage starting & stopping the server ###

@restlite.resource
def start_handler():
  global authmodel
  def GET(request):
    if authmodel:
      authmodel.login(request)
    return request.response("Got a start GET request")
  
  def POST(request, entity):
    if authmodel:
      authmodel.login(request)
      
    os_call_to_vazels = runVazels()
      
    if os_call_to_vazels is True :
      # Need to upadate client to handle 204 as a successful response
      return request.response("")
    else :
      raise restlite.Status("500 "+str(os_call_to_vazels))
    
  return locals()

@restlite.resource
def stop_handler():
  global authmodel
  def GET(request):
    if authmodel:
      authmodel.login(request)
    return request.response("Got a stop GET request")
  
  def POST(request,entity):
    if authmodel:
      authmodel.login(request)
    
    os_call_to_vazels = stopVazels()
    
    if os_call_to_vazels is True :
      return request.response("")
    else :
      raise restlite.Status("500 "+str(os_call_to_vazels))
      
  return locals()

### Allows for submitting groups ###

''' URIs of the form /groups/$ are used for batch handling, whereas
those of the form /groups/somethingMore are used for individual groups'''

@restlite.resource
def group_batch_handler():
  global authmodel
  
  ## GET requests to this uri will return a summary of current groups
  def GET(request):
    if authmodel:
      authmodel.login(request)
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
    if authmodel:
      authmodel.login(request)
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
  global authmodel
  
  ## GET requests are for grabbing current info on a group
  def GET(request):
    if authmodel:
      authmodel.login(request)
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

### Serves authorised pages from global.pagedir ###

@restlite.resource
def auth_page_handler():
    global authmodel
    import os
    def GET(request):
        if authmodel:
            authmodel.login(request)
        if '..' in request['PATH_INFO']:
            raise restlite.Status, '400 Invalid Path'
        page_file = os.path.join(
            config.getSettings("global")["basedir"],
            config.getSettings("server")["pagedir"],
            request['PATH_INFO']
        )
        try:
            return request.response(getPage(page_file), getType(page_file))
        except restlite.Status:
            index_page = os.path.join(page_file, 'index.html')
            return request.response(getPage(index_page), 'text/html')

    def getPage(path):
        try:
            with open(path, 'r') as page:
                return page.read()
        except IOError:
            raise restlite.Status, "404 Not Found"

    def getType(path):
        extSplit = path.rsplit('.', 1)
        ext = extSplit[len(extSplit)-1]
        try:
            return {
                'html' : 'text/html',
                'htm' : 'text/html',
                'css' : 'text/css',
                'js' : 'text/javascript',
            }[ext]
        except KeyError:
            return 'text/plain'

    return locals()



### Allows Editing of config settings through /settings ###
#
# GET
# - /settings/ returns a list of settings types
# - /settings/section returns a json representation of this section of the settings
# POST
# - /settings/ gives error
# - /settings/section?name_s=value&name2_i=value2&name3&...
#   Updates section with the names and values given,
#   To delete a setting add _r to it's name
#   To add/edit a setting, use it's name appended with _ and a character 
#   to specify the type. Obviously add the value too after the '='.
#   Types:
#   - s : String
#   - i : Integer
#   - d : Float
#   - If none it is ignored
#   Only use each name once in an update
#   otherwise only the first one will be counted

#
# JUST SO YOU KNOW, THIS IS NOT CURRENTLY USED
#


@restlite.resource
def settings_editor():
    global authmodel
    def GET(request):
        if authmodel:
            authmodel.login(request)
        section = getSection(request['PATH_INFO'])
        if section:
            settings = config.getSettings(section, False)
            if settings is None:
                raise restlite.Status, "404 Not Found"
            else:
                return request.response(settings)
        else:
            return request.response(config.getSections())

    def POST(request, entity):
        if authmodel:
            authmodel.login(request)
        section = getSection(request['PATH_INFO'])
        # Uncomment below to disallow editing of new sections
        #if not section:
        #    raise restlite.Status, '405 Method Not Allowed'

        # Grab arguments
        import urlparse
        parsed = urlparse.parse_qs(entity, True) # Null value is kept as ''
        used_names = [] # Names used
        new_qs = {} # Values to add
        rem_qs = [] # Values to delete

        for key in parsed:
            # Grab name and extension
            name, ext = splitName(key)
            # If name already used, ignore
            if name in used_names:
                continue
            # If theres no extension ignore
            if ext is None:
                continue
            # If there is no value at all, skip
            if parsed[key] == []:
                continue
            # Otherwise take first value
            q = parsed[key][0]
            # If up for deletion
            if ext is 'r':
                # Check used_names here as we use key when deleting
                # Or parsed name otherwise below
                rem_qs += [name]
                used_names += [name]
            # Adding value
            else:
                # Try to convert to the correct format
                func = funcFor(ext)
                if func is None:
                    raise restlite.Status, "405 Invalid Method"
                try:
                    q = func(q)
                except:
                    raise restlite.Status, "405 Invalid Value"
                new_qs[name] = q
                used_names += [name]

        # All settings are good to go
        settings = config.getSettings(section)
        # Add all new settings
        settings.update(new_qs)
        # Delete all new deletions
        for key in rem_qs:
            try:
                del settings[key]
            except KeyError: pass
        # Make it permanent
        config.saveConfig()
        return request.response(settings)

    def splitName(name):
    # Returns name and letter indicating type
    # If name ends in _. where . is any character, return (name, func)
    # If there is no _ give full name as name and None type
        l = len(name)
        if l > 1 and name[l-2] == '_':
            return name[:(l-2)], name[l-1]
        else:
            return name, None

    def funcFor(ext):
        fs = {
            's' : str,
            'i' : int,
            'd' : float,
        }
        try: return fs[ext]
        except KeyError: return None
        

    def getSection(path):
        return path.split('/')[0]

    return locals()


### Brings up a console on the server to inspect the request ###

@restlite.resource
def test_handler():
    import code
    def GET(request):
        code.InteractiveConsole(locals()).interact()        
        return request.response('', 'text/plain')

    def POST(request, entity):
        code.InteractiveConsole(locals()).interact()
        return request.response('', 'text/plain')
        
    return locals()
