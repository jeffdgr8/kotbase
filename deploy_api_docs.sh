#!/bin/bash

#
# Copyright 2024 Jeff Lockhart
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -ex

TEMP_DIR=temp-clone
PAGES_BRANCH=gh-pages

rm -rf $TEMP_DIR
git clone . $TEMP_DIR
cd $TEMP_DIR

git checkout $PAGES_BRANCH

rm -rf api
cp -r ../docs/api api
git add .

git commit -m "Publish API docs"

cd ..
rm -rf $TEMP_DIR

git push origin $PAGES_BRANCH
