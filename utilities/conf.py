import pickle
import sys
import code
import os

if len(sys.argv) < 2:
    print "Need a project to open the config file for"
else:
    path = os.path.join(
        os.path.dirname(sys.argv[0]),
        os.path.pardir,
        "deploy",
        "pro_"+sys.argv[1],
        "reef.config"
    )
    conf = pickle.load(open(path))
    console = code.InteractiveConsole({'conf':conf})
    import readline
    console.interact("The config is called conf")
