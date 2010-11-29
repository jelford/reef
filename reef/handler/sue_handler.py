# Module for grabbing parts of the SUE and assigning them to groups.

import restlite
import config
import authentication
import os


config.getSettings("SUE").setdefault("dir","SUE")
config.getSettings("SUE").setdefault("defs", {})

### Assignment of SUE components to groups ###
# GETting will return which SUE component names are assigned to which groups
# POSTing a file/componentname with a group name (/SUE/groupname/) will assign
#   that component to that group with that label

@restlite.resource
def SUE_handler():
  def GET(request):
    authentication.login(request)
    groups = config.getSettings("groups")
    
    component_info = {}
    
    # For each component, build a list of groups to which it is assigned.
    for component_name in config.getSettings("SUE")[defs]:
      assigned_groups = []
      for group in groups:
        if groups[group]["sue_components"].indexof(component_name) != -1:
          assigned_groups.append(group)
      component_info[component_name] = assigned_groups
    
    return request.response(component_info)
      
  def POST(request,entity):
    authentication.login(request)
    
    groups = config.getSettings("groups")
    
    fields = parseMultipart(request, entity)
    if fields is None:
      raise restlite.Status, "400 Invalid SUE POST request"
    
    # Try to find out which group we're sending to
    group_name = fields.getfirst("group_name")
    if not group_name:
      group_name = request['PATH_INFO']
      
    # Try to find what we should label the component
    component_name = fields.getfirst("component_name")
    if not component_name:
      raise restlite.Status, "400 Must give the SUE component a name"
    
    # If we can't find the group then throw
    if not group_name in groups:
      raise restlite.Status, "400 Invalid group given for SUE request"
    
    # Try to get the SUE component
    try:
      component_f = fields['component_file']
      if component_f.file:
        component = component_f.file.read()
    except KeyError:
      raise restlite.Status, "400 Must provide a file when specifying a SUE component"
    
    saveSueComponent(component_name, group_name, component)
    
    return request.response("", "text/plain")
  
  return locals()
  
  
  
def parseMultipart(request, entity):
    import cgi
    import StringIO
    # Dicts for the entity parser to work
    head = {
        'content-type' : request['CONTENT_TYPE'],
        'content-length' : request['CONTENT_LENGTH'],
    }

    env = {'REQUEST_METHOD' : 'POST'}

    return cgi.FieldStorage(
        fp=StringIO.StringIO(entity),
        headers=head,
        environ=env
    )
    

# Save a SueComponent to a new file and add to naming list
def saveSueComponent(component_name, group_name, component_file):
    import tempfile
    sue_dir = os.path.join(
        config.getSettings("global")["projdir"],
        config.getSettings("SUE")["dir"]
    )

    ensureDir(sue_dir)

    temp_f = tempfile.mkstemp(
        suffix='.tar.gz',
        prefix='',
        dir=sue_dir
    )

    try:
        temp = os.fdopen(temp_f[0], 'wb')
        temp.write(component_file)
    finally:
        temp.close()

    newSueComponent = {
        "name" : component_name,
        "file" : temp_f[1],
    }

    if component_name in config.getSettings("SUE")["defs"]:
        os.remove(config.getSettings("SUE")["defs"][component_name]["file"])
    config.getSettings("SUE")["defs"][component_name] = newSueComponent
    
    config.getSettings("groups")[group_name]["sue_components"].append(component_name)
    config.saveConfig()


def ensureDir(dir):
    if not os.path.isdir(dir):
        os.makedirs(dir)
