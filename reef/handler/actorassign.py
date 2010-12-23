"""
:synopsis: Handler to allow assignment of actors to workloads.

"""

import restlite
import config
import authentication

from handler.dochandler import DocHandler

import handler.workloads # to grab defaults
#config.getSettings("workloads").setdefault("dir","workloads")
#config.getSettings("workloads").setdefault("defs",{})

class ActorAssignHandler(DocHandler):
    """Documented handler for assignment of actors to workloads."""

    def POST(self, request, entity):
        """
        (Un)assign actors (from/)to workloads.

        The parameters listed are part of the url encoded ``entity``.

        :param do: What to do, either ``"add"`` or ``"rem"`` to add or remove
                   respectively
        :param workload: The name of the workload to affect.
        :param actor: The name of the actor to add or remove.
        :raises: :exc:`restlite.Status` 400 if a parameter is given multiple
                 times.

                 :exc:`restlite.Status` 400 if a parameter is missing.

                 :exc:`restlite.Status` 404 if the actor or workload does
                 not exist.

                 :exc:`restlite.Status` 400 if the action (``do``) is invalid.

                 :exc:`restlite.Status` 409 if the action cannot be completed
                 do to the state of currently attached workloads.
        :returns: The JSON representation of the workload.

        """

        authentication.login(request)

        import urlparse
        args = urlparse.parse_qs(entity, True)

        # Only accept a single value for any parameter
        for key in args:
            if len(args[key]) > 1:
                raise restlite.Status, "400 Duplicate Arguments"
            args[key] = args[key][0]

        # Check for action
        if "do" not in args:
            raise restlite.Status, "400 Missing Action"

        # Check for workload
        if "workload" not in args:
            raise restlite.Status, "400 Missing Workload"

        # Check for actor
        if "actor" not in args:
            raise restlite.Status, "400 Missing Actor"

        do = args["do"].lower()
        workload = args["workload"]
        actor = args["actor"]

        # Validate workload
        if workload not in config.getSettings("workloads")["defs"]:
            raise restlite.Status, "404 Workload Not Found"

        # Validate actor
        if actor not in config.getSettings("actors")["defs"]:
            raise restlite.Status, "404 Actor Not Found (" +actor+ ")"

        workload = config.getSettings("workloads")["defs"][workload]

        try:
            {
                "add" : self.addActor,
                "rem" : self.remActor,
            }[do](workload, actor)
        except KeyError:
            raise restlite.Status, "400 Invalid Action"            

        config.saveConfig()

        return request.response(workload)


    def addActor(self, workload, actor):
        """
        Add an actor to the given workload.

        :param workload: The workload (from :mod:`config`) to add to.
        :type workload: ``dict``
        :param actor: The name of the actor to add.
        :type actor: ``str``
        :raises: :exc:`restlite.Status` 409 if the actor is already attached.

        """

        if actor in workload["actors"]:
            raise restlite.Status, "409 Actor Already Attached"
        workload["actors"].append(actor)

    def remActor(workload, actor):
        """
        Remove an actor from the given workload.

        :param workload: The workload (from :mod:`config`) to remove from.
        :type workload: ``dict``
        :param actor: The name of the actor to remove.
        :type actor: ``str``
        :raises: :exc:`restlite.Status` 409 if the actor to remove is
                 not attached.

        """

        if actor not in workload["actors"]:
            raise restlite.Status, "409 Cannot Remove Non-Attached Actor"
        workload["actors"].remove(actor)
