import sys, os
import config
import atexit
import permanent_bits as pb

# Stuff for writing messages easily

def show_break():
    print ""
    print "------------------"
    print ""

# Welcome message

print ""
print "Welcome to " + pb.appname_long + "! The web GUI for Vazels."
print ""
print pb.app_desc

show_break()

# Set up base directories

base_dir = os.path.join(os.path.dirname(sys.argv[0]),os.path.pardir)
base_dir = os.path.abspath(base_dir)

proj_dir = os.path.abspath(os.curdir)
print "Using project directory: " + proj_dir
print "If you're not happy with this then press CTRL+C to kill me and run me from somewhere better"
print "Otherwise you can hit return to carry on..."

try:
    raw_input()
except KeyboardInterrupt:
    print "\nBye Bye..."
    sys.exit()

print "Oh good, let's continue then"

show_break()

# Load / Create config

config_file = os.path.join(proj_dir, pb.appname_file+".config")
print "Using " + config_file + " as the config file."

config.setConfig(config_file)

new_config = not config.onDisk()

config.getSettings("global")["basedir"] = base_dir

if new_config:
    config.getSettings("global")["projdir"] = proj_dir
else:
    print "Config Loaded"

show_break()

# Security Setup

# Has to be imported after config is set up
import security

if not config.getSettings("security")["customkey"]:
    print "Creating new SSH key..."
    security.makeKey()

print "Backing up and authorising custom key..."
security.backup()
security.authoriseKey()
atexit.register(security.restore)
print "Done."

show_break()

# Pre-Server Launch Setup

if new_config:
# Ask only if we're looking at a new file
    from getpass import getpass
    import authentication

    password = getpass("Please give a passkey for accessing the interface (Blank for none): ")
    if password:
        authentication.addUser(pb.app_user, password)
    else:
        authentication.clear()

    show_break()

# Start server

import myserver

myserver.getServer().setup()

print "Now you can log on to the server with the following credentials:"
print ""
print "Location: http://localhost:" + str(config.getSettings("server")["port"])
if config.getSettings("server")["passkey"]:
    print "User: " + pb.app_user
    print "Password: <You should know>"
print ""
print "If I displayed the password back to you then the small amount of security during the input would be wasted."
print "(You can just read it in the config file though, that's a problem.)"

show_break()

config.saveConfig()
print "Config written to disk"

# Set automatic save on close
atexit.register(config.saveConfig)

show_break()

print "Starting server...press CTRL+C to stop."

# This lets the server be stopped with CTRL+C
# As it runs to finish, exit handlers are called
try:
    myserver.getServer().start()
except KeyboardInterrupt: pass
