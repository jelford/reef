"""
:synopsis: Use :mod:`restlite` server handlers but preserve documentation.

When the :func:`restlite.resource` decorator is used to create handlers,
it changes the module the handler is in, and as a result the handler is
not documented. This should help keep any docstrings intact and in module.

"""

import restlite

class DocHandler:
    """
    Any handlers that want to preserve their documentation should
    extend this class. They can overwrite the GET and POST methods
    to provide functionality.

    """

    def GET(self, request):
      """
      Handle a GET request.

      This is a dummy. Override it to actually handle GET requests.

      :param request: The request data
      :type request: :class:`restlite.Request`
      :returns: A response from :func:`restlite.Request.response`.
      :rtype: ``str``

      """

    def POST(self, request, entity):
      """
      Handle a POST request.

      This is a dummy. Override it to actually handle POST requests.

      :param request: The request data
      :type request: :class:`restlite.Request`
      :param entity: The POST entity.
      :type entity: ``str`` or an object...
      :returns: A response from :func:`restlite.Request.response`.
      :rtype: ``str``

      """

    def _getOverriddenFuncs(self):
        """
        Get the functions that have overridden the defaults.

        This is used as the function for :func:`restlite.resource` to
        decorate.

        :returns: A dict of the functions we have overridden.
        :rtype: ``dict``

        """

        funcs = {}

        if self.GET.im_class != DocHandler:
            funcs["GET"] = self.GET
        if self.POST.im_class != DocHandler:
            funcs["POST"] = self.POST

        return funcs


    def getHandler(self):
        """
        Create and return the :mod:`wsgiref` compatible handler that this
        handler represents. This will use :func:`restlite.resource`.

        :returns: A :mod:`wsgiref` compliant handler.
        :rtype: ``function``

        """

        import restlite
        return restlite.resource(self._getOverriddenFuncs)
