#!/usr/bin/python
import os

oldVersion = ""
newVersion = "sss"
#result = os.popen("git rev-list " + oldVersion + ".." + newVersion).read()
result = os.popen("git show").read()
print result
