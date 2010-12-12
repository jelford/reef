from restlite import tojson

# Defined a set class that will serialize nicely so that it Python won't cry
#   when we try to return it to the client
    
class DynamicDict(dict):
  def _json_ (self) :
    return {"name" : self["name"],
            "size" : self["size"],
            "workloads" : list(self["workloads"]),
            "filters" : list(self["filters"]),
            "online_hosts" : list(self["online_hosts"]),
            "evolving_hosts" : list(self["evolving_hosts"])
            }