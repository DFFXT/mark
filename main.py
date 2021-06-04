#!/usr/bin/python
import os

oldVersion = ""
newVersion = "sss"

result = os.popen("git show").read()
print result
