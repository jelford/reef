"""
:synopsis: Handles most actor related requests.

"""

import restlite
import config
import os
import authentication

from handler.dochandler import DocHandler

config.getSettings("actors").setdefault("dir","actors")
config.getSettings("actors").setdefault("defs",{})

ALLOWED_TYPES = ["java", "python", "sue"]
"""The list of allowed types for uploading."""

class ActorsHandler(DocHandler):
    """Documented handler for dealing with actors."""

    def GET(self, request):
        """
        Get either the list of actors or actor details.

        If the base address of the handler is requested:

        :returns: List of actors on the server.

        If the base / actorname is requested:

        :returns: JSON representation of the actor called actorname
        :raises: :exc:`restlite.Status` 404 if the actor does not exist.

        If the base / actorname.tar.gz is requested:

        :returns: The contents of the actor file.
        :raises: :exc:`restlite.Status` 404 if the actor does not exist.

                 :exc:`restlite.Status` 410 if the actor exists but the
                 file is missing.

                 :exc:`restlite.Status` 500 if the actor file could not
                 be read.

        """

        authentication.login(request)

        actor = request['PATH_INFO']
        namemap = config.getSettings("actors")["defs"]

        # Asking for names of actors
        if not actor:
            return request.response(namemap.keys())

        # Asking for the file
        if actor.endswith(".tar.gz"):
            actorname = actor[:len(actor)-len(".tar.gz")]
            return self.showActorFile(request, actorname)

        # Asking for the actor info
        if actor not in namemap:
            raise restlite.Status, "404 Not Found"

        actordata = namemap[actor].copy()
        del actordata['file']
        return request.response(actordata)

    def POST(self, request, entity):
        """
        Create a new actor.

        You should POST to the base address of the handler although this may
        not be checked. Also the POST should be encoded as mutlipart form
        data.

        The parameters here should be in the POST entity:

        :param actor_name: The name of the actor to add.
        :type actor_name: ``str``
        :param actor_file: The actor's actual file.
        :type actor_file: ``file`` - .tar.gz
        :param actor_type: The type of the actor. This should be one of the
                           values in :data:`ALLOWED_TYPES`.
        :type actor_type: ``str``
        :returns: The name of the modified actor.
        :raises: :exc:`restlite.Status` 400 if the data cannot be parsed.

                 :exc:`restlite.Status` 400 if any of the parameters are
                 missing.

                 :exc:`restlite.Status` 400 if the actor type is invalid.

                 :exc:`restlite.Status` 403 if overwriting actors is disabled
                 and you try to use an existing actor name.

        """

        authentication.login(request)

        fields = self.parseMultipart(request, entity)
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
        if actortype not in ALLOWED_TYPES:
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

        self.saveActor(actorname, actortype, actor)
        return request.response(actorname, "text/html")


    def showActorFile(self, request, actorname):
        """
        Try to send a response with the actor file of the given name.

        :param request: The request to create a response from.
        :type request: :class:`restlite.Request`
        :param actorname: The name of the actor from which we take the file.
        :type actorname: ``str``
        :returns: Response with the contents of the actor file.
        :raises: :exc:`restlite.Status` 404 if the actor does not exist.

                 :exc:`restlite.Status` 410 if the actor exists but the
                 file is missing.

                 :exc:`restlite.Status` 500 if the actor file could not
                 be read.

        """

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


    def parseMultipart(self, request, entity):
        """
        Parse the entity.

        This should be easy as python gives us the :mod:`cgi` module to do it,
        but there's a little fiddling to do.

        :param request: The request that the entity came with.
        :type request: :class:`restlite.Request`
        :param entity: The entity to parse.
        :type entity: ``str``
        :returns: The parsed entity.
        :rtype: :mod:`cgi`. ``FieldStorage``

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


    def saveActor(self, name, type, content):
        """
        Save an actor to a new file and add to defs in the :mod:`config`.

        :param name: Name of the actor to add.
        :type name: ``str``
        :param type: Type of the actor to add. This is expected to be in
                     :data:`ALLOWED_TYPES`.
        :type type: ``str``

        """

        import tempfile
        actor_dir = os.path.join(
            config.getSettings("global")["projdir"],
            config.getSettings("actors")["dir"]
        )

        self.ensureDir(actor_dir)

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


    def ensureDir(self, dir):
        """
        Make sure the given directory exists (create it if not).

        :param dir: The absolute path of the directory to check.
        :type dir: ``str``

        """

        if not os.path.isdir(dir):
            os.makedirs(dir)
