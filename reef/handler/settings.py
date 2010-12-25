"""
:synopsis: Quick and easy way to manage simple settings.

"""

import restlite
import config
import authentication

from handler.dochandler import DocHandler

class SettingsEditor(DocHandler):
    """
    Deals with any getting and setting of config files.

    Unfortunately this does not deal with validation or anything other than
    ``str``, ``int`` or ``float`` variables. This is why it's not really
    used anywhere in the application.

    """

    def GET(self, request):
        """
        Get setting data from the :mod:`config`.

        Parameters are expected to be part of the query string.

        If the base of this address handler is called:

        :returns: A list of sections in the :mod:`config`
        :rtype: ``list``

        If anything more than the base of this handler is called then the
        excess is assumed to be a section name and:

        :returns: A JSON representation of that particular section of the 
                  :mod:`config`.
        :rtype: ``json``
        :raises: :exc:`restlite.Status` 404 if the section does not exist.

        """

        authentication.login(request)
        section = getSection(request['PATH_INFO'])
        if section:
            settings = config.getSettings(section, False)
            if settings is None:
                raise restlite.Status, "404 Not Found"
            else:
                return request.response(settings)
        else:
            return request.response(config.getSections())

    def POST(self, request, entity):
        """
        Update a section of the settings.

        Any part of the requested address after the handler's base address will
        be taken as a section name inside the settings. Any parameters are
        assumed to be in the POST entity and url encoded.

        A request can take any number of the following parameters:

        To set a setting:

        :param <setname>_<type>: The value to set this setting to.

        where ``<type>`` is one of:

        * ``"s"`` for string
        * ``"i"`` for integer
        * ``"d"`` for double

        and ``<setname>`` is the name of the setting to set.

        To remove a setting:

        :param <setname>_r: No value

        Only the first occurence of each ``<setname>`` is observed.

        :returns: JSON representation of the settings section.
        :rtype: ``json``
        :raises: :exc:`restlite.Status` 400 if there is a parameter with an
                 invalid action in the name (the bit after the ``"_"``).

                 :exc:`restlite.Status` 400 if the value of a parameter cannot
                 be parsed with the type given.

        """

        authentication.login(request)
        section = getSection(request['PATH_INFO'])
        # Uncomment below to disallow editing of new sections
        #if not section:
        #    raise restlite.Status, '403 New Section Forbidden'

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
                    raise restlite.Status, "400 Invalid Action"
                try:
                    q = func(q)
                except:
                    raise restlite.Status, "400 Invalid Format"
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
    """
    Get a parameter name and a letter indicating an action.

    * If name ends in _. where . is any character, return ``(name, func)``
    * If there is no _ give full name as ``name`` and ``None`` type.

    :param name: The name of a parameter in the request.
    :type name: ``str``
    :returns: A tuple consisting of the parameter name and action character.
    :rtype: ``(str, str)``

    """

    l = len(name)
    if l > 1 and name[l-2] == '_':
        return name[:(l-2)], name[l-1]
    else:
        return name, None


def funcFor(ext):
    """
    Get a parse function corresponding to an action character.

    :param ext: The action character.
    :type ext: ``str``
    :returns: A parser function or ``None`` if there is not one matching.
    :rtype: ``func``
    """

    fs = {
        's' : str,
        'i' : int,
        'd' : float,
    }
    try: return fs[ext]
    except KeyError: return None

def getSection(path):
    """
    Pull out the section name from the path.

    This means anything after the first "/".

    :param path: The requested path.
    :type path: ``str``
    :returns: A section name (possibly non-existent in the settings).
    :rtype: ``str``
    """

    return path.split('/')[0]

