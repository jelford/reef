import config
import os


def getVazelsPath():
  return os.path.join(config.getSettings("global")['basedir'],"vazels")