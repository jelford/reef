"""
:synopsis: Deals with any group related requests.

"""

import config
import restlite
import urlparse
import authentication

from handler.dochandler import DocHandler

class Group(dict):
    """
    A class version of the old ``dict`` representation of a group.

    This way Python won't cry when we try to serialise it as JSON.

    We expect this class to contain the following ``dict`` attributes:

    * ``name`` : The name of the group.
    * ``size`` : The size of the group.
    * ``workloads`` : A list of attached workloads.
    * ``filters`` : A list of relevant(?) mapping restrictions.
    * ``online_hosts`` : List of hostnames for connected hosts.
    * ``evolving_hosts`` : List of hostnames for evolving hosts.

    """

    def __init__(self, name):
        self["name"] = name # We name all groups
        self["size"] = 0 # Need to store the size
        self["workloads"] = set([]) # Will store a list of workload names
        self["filters"] = set([]) # Store a list of mapping restrictions (currently non-functional)
        self["online_hosts"] = set([]) # Store a list of host names for connected & evolving hosts
        self["evolving_hosts"] = set([])  
        self["sue_components"] = set([])

    def _json_(self):
        return {"name" : self["name"],
                "size" : self["size"],
                "workloads" : list(self["workloads"]),
                "filters" : list(self["filters"]),
                "online_hosts" : list(self["online_hosts"]),
                "evolving_hosts" : list(self["evolving_hosts"])
                "sue_components" = list(self["sue_components"])
                }


class GroupBatchHandler(DocHandler):
    """Handles requests for general group information."""

    def GET(self, request):
        """
        Get the list of groups on the server.

        :returns: A list of groups.
        :rtype: ``list``

        """

        authentication.login(request)
        group_data = config.getSettings("groups").keys()

        return request.response(group_data)


class GroupHandler(DocHandler):
    """Handles requests for individual groups."""

    def GET(self, request):
        """
        Get the JSON representation of a specific group.

        The group name is taken to be anything in the requested path after
        the base address of the handler.

        :returns: JSON representation of the requested group.
        :rtype: :class:`Group`
        :raises: :exc:`restlite.Status` 404 if the group requested was not
                 found.

        """

        authentication.login(request)

        group_name = request['PATH_INFO']

        try:
            group_settings = config.getSettings("groups",True)[group_name]
            return request.response(group_settings)
        except KeyError:
            raise restlite.Status, "404 Group Not Found"

    def POST(self, request, entity):
        """
        Create a new or edit an existing group.

        The group name is taken to be anything in the requested path after
        the base address of the handler.        

        Parameters are expected to be part of the POST entity.

        :param size: Group size.
        :type size: ``int``
        :param workloads: Can be included any number of times, each should be
                          the name of an attached workload.
        :type workloads: ``str``
        :param filters: Can be included any number of times, each should be a
                        mapping restriction.
        :type filters: ``str``
        :returns: JSON representation of the new state of the group.
        :rtype: ``dict``
        :raises: :exc:`restlite.Status` 400 if the size is invalid.

                 :exc:`restlite.Status` 400 unrecognised parameters are
                 included.

        """

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
            n_group = Group(group_name)
            n_group.update(g_data)
            if n_group["size"] != 0:
                existing_groups[group_name] = n_group

        try:
            return request.response(existing_groups[group_name])
        except KeyError:
            return request.response({"name":group_name, "size":0})



def _getGroupDataFromArgs(args):
    """
    Parse and validate arguments to create a group dictionary.

    The parameter ``args`` should be in the format returned by
    :func:`urlparse.parse_qs`. It can have any of the following:

    :param size: Group size.
    :type size: ``int``
    :param workloads: Attached workloads.
    :type workloads: ``list(str)``
    :param filters: Mapping restrictions.
    :type filters: ``list(str)``
    :returns: A dictionary with extracted group data.
    :rtype: ``dict``
    :raises: :exc:`restlite.Status` 400 if the size is invalid.

             :exc:`restlite.Status` 400 if unrecognised arguments are included.
    """

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


def getGroupFromRank(rank):
    """
    Find a group from it's rank (given by vazels).

    Don't call this during the setup phase; it doesn't make sense!

    :param rank: The rank to find the group for.
    :type rank: ``int``
    :returns: The group with the given rank.
    :rtype: :class:`Group`
    :raises: :exc:`restlite.Status` 404 if the group rank doesn't exist.
    """

    # Don't call this during the setup phase; it doesn't make sense!
    group_data = config.getSettings("groups",True)

    for group_name in group_data:
        if group_data[group_name]['group_number'] == rank:
            return group_data[group_name]

    raise restlite.Status, "404 Tried to find physical group number which doesn't exist"
