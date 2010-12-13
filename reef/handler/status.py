import restlite
import authentication

# Ping this to find out whether the server is up and running.
#  If you don't get a response from this, you're in trouble.
@restlite.resource
def status_handler():
  def GET(request):
    authentication.login(request)
    return restlite.response("")
    
  return locals()