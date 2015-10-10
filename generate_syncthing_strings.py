#!/usr/bin/env python

import json
import sys
import os
from collections import OrderedDict
import re
import string

base_path = "syncthing/src/github.com/syncthing/syncthing"
langdir = "gui/assets/lang"
resdir = "app/src/main/res"
resfile = "strings-syncthing.xml"

validKeyChars = string.ascii_letters+string.digits+'_'

def fixup(lst):
    nlst = []
    for el in lst:
        key, val = el
        #print(key)
        key = key.lower()
        key = key.replace(' ','_')
        key = key.replace('{%','')
        key = key.replace('%}','')
        key = ''.join(filter(lambda x: x in validKeyChars, key))
        key = re.sub(r'_+',r'_',key)
        key = re.sub(r'^_',r'',key)
        key = re.sub(r'_$',r'',key)
        #print(key)
        #print(val)
        #val = re.sub(r'\{\{.*?\}\}', r'%s',val)
        rep = re.findall(r'\{\{.*?\}\}', val)
        ii=1
        for s in rep:
            val = val.replace(s,'%'+str(ii)+'$s')
            ii+=1
        val = val.replace("'","\\'")
        val = val.replace('"','\\"')
        #print(val)
        nlst.append((key,val))

    return nlst

def sort_dict(d):
    lst = sorted(fixup(d.items()), key=lambda x: x[0])
    return OrderedDict(lst)

def write_opening(f):
    f.write('<?xml version="1.0" encoding="utf-8"?>\n')
    f.write('<!--AUTO GENERATED RESOURCE DO NOT EDIT!!!!-->\n')
    f.write('<resources>\n')

def write_closing(f):
    f.write('</resources>')

def write_xml(json, directory):
    with open(os.path.join(directory,resfile),mode='w', encoding="utf-8") as f:
        write_opening(f)
        for key in json.keys():
            val = json.get(key)
            f.write('    <string name="%s">%s</string>\n' % (key,val))
        write_closing(f)

for root, dirs, files in os.walk(resdir):
    for name in files:
        if name == resfile:
            path = os.path.join(root, name)
            print("removing " + path)
            os.remove(path)

realpath = os.path.join(base_path, langdir);
print(realpath)
for f in os.listdir(realpath):
    langfile = os.path.join(realpath, f)
    if not langfile.endswith("json") or "@" in langfile:
        continue
    print(langfile)
    with open(os.path.join(base_path,langdir,f), encoding="utf-8") as jf:
        j = json.load(jf);
    if f.endswith("en.json"):
        f = "lang"
    f = re.sub(r'(lang-\w.)-(\w.)\.json',r'\1-r\2.json',f)
    xmldir=os.path.join(resdir,f.replace("lang","values").replace(".json", ""))
    print(xmldir)
    if not os.path.exists(xmldir):
        os.makedirs(xmldir);
    write_xml(sort_dict(j),xmldir)

