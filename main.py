# MkDocs Macros https://mkdocs-macros-plugin.readthedocs.io/

import re

def define_env(env):
    # Define version_short variable, e.g. 3.0 from version 3.0.12-1.0.0
    version = env.variables.version_full
    match = re.match(r"\d+\.\d+", version)
    env.variables.version_short = match.group(0)
