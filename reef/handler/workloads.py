import restlite
import config
import authentication
import os

### Accepts workload files or text fields ###
#
# GETting the base of this handler gives a list of workloads
# GETting more than the base gives workload data by that name
# GETting the base / name .wkld gives the workload file
# POSTing can create a new workloads
# - wkld_name gives the name
# - wkld_file gives the file
# - wkld_text gives the test
# wkld_text has priority over wkld_file

config.getSettings("workloads").setdefault("dir","workloads")
config.getSettings("workloads").setdefault("defs",{})

@restlite.resource
def workload_handler():
    def GET(request):
        authentication.login(request)

        wkld = request['PATH_INFO']
        namemap = config.getSettings("workloads")["defs"]

        # Asking for names of workloads
        if not wkld:
            return request.response(namemap.keys())

        # Asking for workload file
        if wkld.endswith(".wkld"):
            wkldname = wkld[:len(wkld)-len(".wkld")]

            # Asking for a non existent workload
            if wkldname not in namemap:
                raise restlite.Status, "404 Not Found"

            # Asking for an existing workload
            filename = namemap[wkldname]["file"]

            # File gone?
            if not os.path.isfile(filename):
                raise restlite.Status, "410 Resource Missing"

            with open(filename, 'rb') as file:
                return request.response(file.read(), 'text/plain')

            # Something went wrong
            raise restlite.Status, "500 Resource Unreadable"

        # Otherwise get wkld info
        if wkld not in namemap:
            raise restlite.Status, "404 Not Found"

        wkld = namemap[wkld].copy()
        del wkld["file"]
        return request.response(wkld)

    def POST(request, entity):
        authentication.login(request)

        fields = parseMultipart(request, entity)
        if fields is None:
            raise restlite.Status, "400 Invalid Request"

        # Try to pull a name
        wkldname = fields.getfirst("wkld_name")
        if not wkldname:
            raise restlite.Status, "400 No Workload Name"
        # Uncomment below to ban editing old workloads
        #elif wkldname in config.getSettings("workloads")["defs"]:
        #    raise restlite.Status, "403 Name Reuse Forbidden"

        # Try to get text for the workload
        wkld = fields.getfirst("wkld_text")
        if wkld is None:
            # Couldn't so try to get text from file
            try:
                wkld_f = fields['wkld_file']
                if wkld_f.file:
                    wkld = wkld_f.file.read()
            except KeyError: pass

        # Still couldn't get text
        if wkld is None:
            raise restlite.Status, "400 No Workload Supplied"

        saveWorkload(wkldname, wkld)
        return request.response(wkldname, "text/plain")

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
    

# Save a workload to a new file and add to naming list
def saveWorkload(name, content):
    import tempfile
    wkld_dir = os.path.join(
        config.getSettings("global")["projdir"],
        config.getSettings("workloads")["dir"]
    )

    ensureDir(wkld_dir)

    temp_f = tempfile.mkstemp(
        suffix='.wkld',
        prefix='',
        dir=wkld_dir
    )

    try:
        temp = os.fdopen(temp_f[0], 'wb')
        temp.write(content)
    finally:
        temp.close()

    newWorkload = {
        "name" : name,
        "file" : temp_f[1],
        "actors" : []
    }

    if name in config.getSettings("workloads")["defs"]:
        os.remove(config.getSettings("workloads")["defs"][name]["file"])
    config.getSettings("workloads")["defs"][name] = newWorkload
    config.saveConfig()


def ensureDir(dir):
    if not os.path.isdir(dir):
        os.makedirs(dir)
