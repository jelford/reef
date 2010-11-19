import restlite
import config
import handlers
import os

### Accepts actor tar.gz files ###
#
# GETting the base of this handler gives a list of actors
# GETting more of the base gives one of:
# - If the extension is just the actor name, the actor data
# - If the extension is the actor name + .tar.gz, the actor file
# POSTing can create a new actors
# - actor_name gives the name
# - actor_file gives the file
# - actor_type gives the type (either JAVA or PYTHON)

config.getSettings("actors").setdefault("dir","actors")
config.getSettings("actors").setdefault("defs",{})
allowed_types = ["java", "python", "sue"]

@restlite.resource
def actors_handler():
    def GET(request):
        handlers.login(request)

        actor = request['PATH_INFO']
        namemap = config.getSettings("actors")["defs"]

        # Asking for names of actors
        if not actor:
            return request.response(namemap.keys())

        # Asking for the file
        if actor.endswith(".tar.gz"):
            actorname = actor[:len(actor)-len(".tar.gz")]
            return showActorFile(request, actorname)

        # Asking for the actor info
        if actor not in namemap:
            raise restlite.Status, "404 Not Found"

        actordata = namemap[actor].copy()
        del actordata['file']
        return request.response(actordata)

    def POST(request, entity):
        handlers.login(request)

        fields = parseMultipart(request, entity)
        if fields is None:
            raise restlite.Status, "400 Invalid Request"

        # Try to pull a name
        actorname = fields.getfirst("actor_name")
        if not actorname:
            raise restlite.Status, "400 No Actor Name"
        # Uncomment below to ban editing old actors
        #elif actorname in config.getSettings("actors")["defs"]:
        #    raise restlite.Status, "403 Name Reuse Forbidden"

        # Try to pull type
        actortype = fields.getfirst("actor_type")
        if not actortype:
            raise restlite.Status, "400 No Actor Type"

        actortype = actortype.lower()
        if actortype not in allowed_types:
            raise restlite.Status, "400 Invalid Actor Type"

        # Try to get file
        actor = None
        try:
            actor_f = fields['actor_file']
            if actor_f.file:
                actor = actor_f.file.read()
        except KeyError: pass

        # Couldn't get actor
        if not actor:
            raise restlite.Status, "400 No Actor Supplied"

        saveActor(actorname, actortype, actor)
        return request.response("", "text/plain")

    return locals()


def showActorFile(request, actorname):
    namemap = config.getSettings("actors")["defs"]

    # Asking for a non existent actor
    if actorname not in namemap:
        raise restlite.Status, "404 Not Found"

    filename = namemap[actorname]['file']

    # File gone?
    if not os.path.isfile(filename):
        raise restlite.Status, "410 Resource Missing"

    # Asking for an existing actor
    with open(filename, 'rb') as file:
        return request.response(file.read(), 'application/x-gzip')

    # Something went wrong
    raise restlite.Status, "500 Resource Unreadable"


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
    

# Save an actor to a new file and add to defs
def saveActor(name, type, content):
    import tempfile
    actor_dir = os.path.join(
        config.getSettings("global")["projdir"],
        config.getSettings("actors")["dir"]
    )

    ensureDir(actor_dir)

    temp_f = tempfile.mkstemp(
        suffix='.tar.gz',
        prefix='',
        dir=actor_dir
    )

    try:
        temp = os.fdopen(temp_f[0], 'wb')
        temp.write(content)
    finally:
        temp.close()

    newActor = {
        "name" : name,
        "type" : type,
        "file" : temp_f[1]
    }

    if name in config.getSettings("actors")["defs"]:
        os.remove(config.getSettings("actors")["defs"][name]["file"])
    config.getSettings("actors")["defs"][name] = newActor
    config.saveConfig()


def ensureDir(dir):
    if not os.path.isdir(dir):
        os.makedirs(dir)
