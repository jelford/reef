#!/usr/bin/env python

"""
:synopsis: Entry point for the Reef server.

To run the server execute this script. It can also be safely imported into
other modules however there's not much use for that other than to document the
module.

"""

import sys, os
import config
import atexit
import permanent_bits as pb
# Do not add things here that will set default
# If do you, they will be overwritten when config is loaded


def show_break():
    """Display a break in the text output."""

    print ""
    print "------------------"
    print ""


def windowsWarning():
    """
    Warn if the user is running windows and quit.

    Don't even bother loading on Windows - there's no point.

    """

    import platform

    if platform.system == 'Windows':
        sys.exit('It looks like you\'re trying to run this program on' +
                 ' the "Microsoft Windows" operating system - Vazels' +
                 ' won\'t run there.')


def showWelcome():
    """Show a welcome message for the reef server."""

    print ""
    print "Welcome to " + pb.appname_long + "! The web GUI for Vazels."
    print ""
    print pb.app_desc


def getBaseDir():
    """
    Get the base directory (the server installation directory).

    :returns: The base directory.
    :rtype: ``str``

    """

    base_dir = os.path.join(os.path.dirname(sys.argv[0]),os.path.pardir)
    base_dir = os.path.abspath(base_dir)

    return base_dir


def getProjDir():
    """
    Get the project directory (the directory we run from).

    :returns: The project directory.
    :rtype: ``str``

    """

    proj_dir = os.path.abspath(os.curdir)
    return proj_dir


def offerDirOrQuit(dir):
    """
    Offer a particular directory as the project directory.

    If the user wishes to decline they can type CTRL+C to quit the application
    cleanly.

    :param dir: The directory to offer.
    :type dir: ``str``

    """

    print "Using project directory: " + dir
    print "If you're not happy with this then press CTRL+C to kill me and run me from somewhere better"
    print "Otherwise you can hit return to carry on..."

    try:
        raw_input()
    except KeyboardInterrupt:
        print "\nBye Bye..."
        sys.exit()

    print "Oh good, let's continue then"


def setupConfig(proj_dir, base_dir):
    """
    Setup the :mod:`config` module using the config file that will be or is in
    the project directory.

    The project and base directories will only be stored if the config is new.

    :param proj_dir: The absolute path to the project directory.
    :type proj_dir: ``str``
    :param base_dir: The absolute path to the base (server installation) 
                     directory.
    :type base_dir: ``str``
    :returns: ``True`` if there was not an existing config file. (i.e. this was
              a new configuration.)
    :rtype: ``bool``

    """

    config_file = os.path.join(proj_dir, pb.appname_file+".config")
    print "Using " + config_file + " as the config file."
    config.setConfig(config_file)

    new_config = not config.onDisk()

    if new_config:
        print "Storing essential directories."
        config.getSettings("global")["basedir"] = base_dir
        config.getSettings("global")["projdir"] = proj_dir
    else:
        print "Config Loaded."

    return new_config


def setupSecurity():
    """
    Set up SSH security ready for Vazels to play with.

    This should be run after any config setup as a module it imports defines a
    number of :mod:`config` defaults and it plays with the configuration itself.

    """

    # Imported here as it sets defaults.
    import security

    if not config.getSettings("security")["customkey"]:
        print "Creating new SSH key..."
        security.makeKey()

    print "Backing up and authorising custom key..."
    security.backup()
    security.authoriseKey()
    atexit.register(security.restore)
    print "Done."


def setupAuthentication():
    """
    Set up :mod:`authentication` ready for the server.

    This should only be run after config setup is complete as one of the
    imported modules provides config defaults.

    """

    import authentication
    from getpass import getpass

    password = getpass("Please give a passkey for accessing the interface (Blank for none): ")
    if password:
        authentication.addUser(pb.app_user, password)
    else:
        authentication.clear()


def setupServer():
    """
    Set up the server and print relevant information.

    This should be called after the config is set up. It gets and sets config
    values.

    """

    import myserver
    import authentication

    myserver.getServer().setup()

    print "Now you can log on to the server with the following credentials:"
    print ""
    print "Location: http://localhost:" + str(config.getSettings("server")["port"])

    if authentication.active():
        print "User: " + pb.app_user
        print "Password: <You should know>"
        print ""
        print "If I displayed the password back to you then the small amount of security during the input would be wasted."
        print "(You can just read it in the config file though, that's a problem.)"


def finaliseConfig():
    """Save config and ask it to save on any normal exit."""

    config.saveConfig()
    print "Config written to disk"

    # Set automatic save on close
    atexit.register(config.saveConfig)


def startServer():
    """Set the server running and allow it to close normally (with no exception)."""

    import myserver

    print "Starting server...press CTRL+C to stop."
    print ""

    # This lets the server be stopped with CTRL+C
    # As it runs to finish, exit handlers are called
    try:
        myserver.getServer().start()
    except KeyboardInterrupt: pass


if __name__ == "__main__":
    windowsWarning()

    showWelcome()
    show_break()

    base_dir = getBaseDir()
    proj_dir = getProjDir()

    offerDirOrQuit(proj_dir)
    show_break()

    new_config = setupConfig(proj_dir, base_dir)
    show_break()

    setupSecurity()
    show_break()

    if new_config:
        # In any other config this would have already been done
        setupAuthentication()
        show_break()

    setupServer()
    show_break()

    finaliseConfig()
    show_break()

    startServer()
    show_break()

    print "All done, see you next time!"
