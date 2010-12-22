"""
:synopsis: Deals with most workload-related requests.

"""

import restlite
import config
import authentication
import os

from handler.dochandler import DocHandler

config.getSettings("workloads").setdefault("dir","workloads")
config.getSettings("workloads").setdefault("defs",{})

class WorkloadHandler(DocHandler):
    """Handles workload related requests."""

    def GET(self, request):
        """
        Get either a list of workloads or details of a specific workload.

        If the base address of this handlers is called:

        :returns: A list of workloads currently uploaded to the server.
        :rtype: ``list(str)``

        If more than the base is called, the extra chunk of the address
        is assumed to be the name of the workload you are requesting and:

        :returns: The JSON representation of the current data for the
                  requested workload.
        :rtype: ``json``
        :raises: :exc:`restlite.Status` 404 if the requested workload does not
                 exist.

        Actually if there is more requested than the base address **and** the
        requested address ends in '.wkld' then we are requesting the file for
        the workload specified before the suffix. In this case:

        :returns: The workload file for the requested workload.
        :rtype: ``text/plain``
        :raises: :exc:`restlite.Status` 404 if the workload is not found.

                 :exc:`restlite.Status` 410 if the recorded file is missing
                 from its expected location.

                 :exc:`restlite.Status` 500 if the file is in the location
                 expected but cannot be read.

        """

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


    def POST(self, request, entity):
        """
        Create a new or update an existing workload.

        Anything requested after the base address of this handler is assumed to
        be the workload name. This is expected to be called at the base address
        of this handler with multipart form data encoding.

        :param wkld_name: The name of the workload being modified.
        :type wkld_name: ``str``
        :param wkld_file: The workload file being uploaded.
        :type wkld_file: As POSTed from a HTML file upload form.
        :param wkld_text: Actual text for the workload being uploaded.
        :type wkld_text: ``str``

        Only one of ``wkld_file`` or ``wkld_text`` have to be given. If both are
        received then the text will take priority.

        :returns: The name of the modifed workload.
        :raises: :exc:`restlite.Status` 400 if the entity could not be parsed.

                 :exc:`restlite.Status` 403 if editing of workload is not
                 permitted and you have tried to reuse a name.

                 :exc:`restlite.Status` 400 if valid workload content could
                 not be found.

        """

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


def parseMultipart(request, entity):
    """
    Try to parse a POST entity encoded as multipart form data.

    :param request: The request data that the entity arrived with.
    :type request: :class:`restlite.Request`
    :param entity: The POST entity to parse.
    :type entity: ``str``
    :returns: The parsed version of the entity, or ``None`` if it cannot be
              parsed.
    :rtype: :mod:`cgi` ``.FieldStorage``

    """

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
    

def saveWorkload(name, content):
    """
    Save a workload to a new file and add it to the naming list.

    :param name: The name of the workload to save.
    :type name: ``str``
    :param content: The content to place in the workload file.
    :type content: ``str``

    """

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
    """
    Make sure a directory exists.

    :param dir: The path to the directory we are ensuring exists.
    :type dir: ``str``

    """

    if not os.path.isdir(dir):
        os.makedirs(dir)
