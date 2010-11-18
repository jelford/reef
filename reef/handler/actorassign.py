import restlite
import config
import authentication

### Assigns actors to workloads ###
#
# POST to assign or unassign an actor from a workload
# - do is add or rem
# - workload is the name of the workload
# - actor is the name of the actor

import handler.workloads # to grab defaults
#config.getSettings("workloads").setdefault("dir","workloads")
#config.getSettings("workloads").setdefault("defs",{})

@restlite.resource
def actorassign_handler():
    def POST(request, entity):
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
                "add" : addActor,
                "rem" : remActor,
            }[do](workload, actor)
        except KeyError:
            raise restlite.Status, "400 Invalid Action"            

        config.saveConfig()

        return request.response(workload)

    return locals()


def addActor(workload, actor):
    if actor in workload["actors"]:
        raise restlite.Status, "409 Actor Already Attached"
    workload["actors"].append(actor)

def remActor(workload, actor):
    if actor not in workload["actors"]:
        raise restlite.Status, "409 Cannot Remove Non-Attached Actor"
    workload["actors"].remove(actor)
