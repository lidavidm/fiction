#! /usr/bin/env python2

import os
import string

template = string.Template(open('template.html').read())

pages = []
for fname in os.listdir('pages'):
    name, ext = os.path.splitext(fname)

    if ext == '.html':
        pages.append(name)

actives = {name + '_active': '' for name in pages}
for name in pages:
    contents = open(os.path.join('pages', name + '.html')).read()
    _actives = actives.copy()
    _actives[name + '_active'] = ' class="active"'
    print "Generating", name + '.html'
    open(name + '.html', 'w').write(template.safe_substitute(content=contents, **_actives))
