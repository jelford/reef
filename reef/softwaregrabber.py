"""
:synopsis: Allows software to be downloaded such as Vazels or the Java runtime.

The idea originally was that this could manage the directories used for the
software too so stubs could be placed here as placeholders. Unfortunately
although there is one stub, I don't think it's used anywhere so this module is
pretty useless.

"""

import config
import os


def getVazelsPath():
    """
    Get the path to the base directory of the Vazels installation.

    :returns: Path to the Vazels installation directory.
    :rtype: ``str``

    """

    return os.path.join(config.getSettings("global")['basedir'],"vazels")
